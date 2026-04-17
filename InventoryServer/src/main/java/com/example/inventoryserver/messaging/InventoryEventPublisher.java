package com.example.inventoryserver.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.eventing.enabled:false}")
    private boolean eventingEnabled;

    public InventoryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishProductCreated(Long productId, String name) {
        if (!eventingEnabled) {
            log.debug("Eventing is disabled. Skipping product creation event for ID #{}", productId);
            return;
        }
        log.info("Publishing Product Created Event for ID #{} ({})", productId, name);
        ProductCreatedEvent event = new ProductCreatedEvent(productId, name);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "product.created", event);
    }

    public record ProductCreatedEvent(Long productId, String name) {}
}
