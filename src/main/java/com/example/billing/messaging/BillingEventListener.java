package com.example.billing.messaging;

import com.example.billing.entity.Product;
import com.example.billing.entity.TaxCategory;
import com.example.billing.repository.ProductRepository;
import com.example.billing.repository.TaxCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class BillingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BillingEventListener.class);
    private final ProductRepository productRepository;
    private final TaxCategoryRepository taxCategoryRepository;

    @Value("${app.eventing.enabled:false}")
    private boolean eventingEnabled;

    public BillingEventListener(ProductRepository productRepository, TaxCategoryRepository taxCategoryRepository) {
        this.productRepository = productRepository;
        this.taxCategoryRepository = taxCategoryRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    public void listenForProductCreated(ProductCreatedEvent event) {
        if (!eventingEnabled) {
            log.debug("Eventing is disabled. Ignoring Product Created Event: {} - {}", event.productId(), event.name());
            return;
        }
        log.info("Received Product Created Event: {} - {}", event.productId(), event.name());
        
        if (!productRepository.existsById(event.productId())) {
            Product product = new Product();
            product.setId(event.productId());
            product.setName(event.name());
            product.setPrice(BigDecimal.ZERO); // Default to zero, must be updated manually in Billing System
            
            Optional<TaxCategory> defaultTax = taxCategoryRepository.findById(1L);
            defaultTax.ifPresent(product::setCategory);
            
            productRepository.save(product);
            log.info("Successfully synced Product #{} ({}) to local database", event.productId(), event.name());
        } else {
            log.debug("Product #{} already exists in local database. Skipping sync.", event.productId());
        }
    }

    public record ProductCreatedEvent(Long productId, String name) {}
}
