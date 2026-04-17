package com.example.billing.service;

import com.example.billing.dto.*;
import com.example.billing.entity.*;
import com.example.billing.messaging.BillingEventPublisher;
import com.example.billing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SettingsService settingsService;
    private final com.example.billing.messaging.BillingEventPublisher billingEventPublisher;

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);


    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository,
                          ProductRepository productRepository, SettingsService settingsService,
                          com.example.billing.messaging.BillingEventPublisher billingEventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.settingsService = settingsService;
        this.billingEventPublisher = billingEventPublisher;
    }

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (InvoiceRequestDTO.Item reqItem : request.getItems()) {
            Product product = productRepository.findById(reqItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + reqItem.getProductId()));
            
            BigDecimal lineTotal = product.getPrice().multiply(new BigDecimal(reqItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            // Compute math
            BigDecimal itemTaxRate = (product.getCategory() != null && product.getCategory().getTaxRate() != null) 
                    ? product.getCategory().getTaxRate() : BigDecimal.ZERO;
            totalTax = totalTax.add(lineTotal.multiply(itemTaxRate));

            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(reqItem.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(lineTotal);
            invoice.addItem(item);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTax(totalTax);
        invoice.setGrandTotal(subtotal.add(totalTax));

        invoice = invoiceRepository.save(invoice);
        
        // Publish SaleEvent via RabbitMQ for asynchronous stock deduction
        if (settingsService.isInventoryEnabled()) {
            log.debug("Inventory integration is enabled. Preparing SaleEvent for invoice #{}", invoice.getId());

            List<com.example.billing.messaging.BillingEventPublisher.SaleItem> deductItems = invoice.getItems().stream()
                .map(item -> new com.example.billing.messaging.BillingEventPublisher.SaleItem(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity()
                )).collect(Collectors.toList());
            
            billingEventPublisher.publishSale(invoice.getId(), deductItems);
        }
        
        return mapToResponseDTO(invoice);
    }
    
    public InvoiceResponseDTO getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return mapToResponseDTO(invoice);
    }

    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private InvoiceResponseDTO mapToResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTax(invoice.getTax());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setDateCreated(invoice.getDateCreated());
        dto.setCustomer(new CustomerDTO(invoice.getCustomer().getId(), invoice.getCustomer().getName(), invoice.getCustomer().getPhone()));
        
        List<InvoiceResponseDTO.Item> items = invoice.getItems().stream().map(item -> {
            InvoiceResponseDTO.Item dtoItem = new InvoiceResponseDTO.Item();
            dtoItem.setId(item.getId());
            dtoItem.setQuantity(item.getQuantity());
            dtoItem.setUnitPrice(item.getUnitPrice());
            dtoItem.setLineTotal(item.getLineTotal());
            Long cid = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getId() : null;
            String cname = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getName() : null;
            BigDecimal crate = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getTaxRate() : null;
            dtoItem.setProduct(new ProductDTO(item.getProduct().getId(), item.getProduct().getName(), item.getProduct().getPrice(), cid, cname, crate));
            return dtoItem;
        }).collect(Collectors.toList());
        
        dto.setItems(items);
        return dto;
    }
}
