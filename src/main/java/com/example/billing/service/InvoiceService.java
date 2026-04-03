package com.example.billing.service;

import com.example.billing.dto.*;
import com.example.billing.entity.*;
import com.example.billing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (InvoiceRequestDTO.Item reqItem : request.getItems()) {
            Product product = productRepository.findById(reqItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + reqItem.getProductId()));
            
            BigDecimal lineTotal = product.getPrice().multiply(new BigDecimal(reqItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(reqItem.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(lineTotal);
            invoice.addItem(item);
        }

        invoice.setSubtotal(subtotal);
        // Assuming a standard 10% tax for the MVP
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        invoice.setTax(tax);
        invoice.setGrandTotal(subtotal.add(tax));

        invoice = invoiceRepository.save(invoice);
        
        return mapToResponseDTO(invoice);
    }
    
    public InvoiceResponseDTO getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return mapToResponseDTO(invoice);
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
            dtoItem.setProduct(new ProductDTO(item.getProduct().getId(), item.getProduct().getName(), item.getProduct().getPrice()));
            return dtoItem;
        }).collect(Collectors.toList());
        
        dto.setItems(items);
        return dto;
    }
}
