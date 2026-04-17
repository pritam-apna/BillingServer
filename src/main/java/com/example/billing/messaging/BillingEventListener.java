package com.example.billing.messaging;

import com.example.billing.entity.Product;
import com.example.billing.entity.TaxCategory;
import com.example.billing.repository.ProductRepository;
import com.example.billing.repository.TaxCategoryRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class BillingEventListener {

    private final ProductRepository productRepository;
    private final TaxCategoryRepository taxCategoryRepository;

    public BillingEventListener(ProductRepository productRepository, TaxCategoryRepository taxCategoryRepository) {
        this.productRepository = productRepository;
        this.taxCategoryRepository = taxCategoryRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    public void listenForProductCreated(ProductCreatedEvent event) {
        System.out.println("Received Product Created Event: " + event.productId() + " - " + event.name());
        
        if (!productRepository.existsById(event.productId())) {
            Product product = new Product();
            product.setId(event.productId());
            product.setName(event.name());
            product.setPrice(BigDecimal.ZERO); // Default to zero, must be updated manually in Billing System
            
            Optional<TaxCategory> defaultTax = taxCategoryRepository.findById(1L);
            defaultTax.ifPresent(product::setCategory);
            
            productRepository.save(product);
            System.out.println("Synced Product to DB");
        }
    }

    public record ProductCreatedEvent(Long productId, String name) {}
}
