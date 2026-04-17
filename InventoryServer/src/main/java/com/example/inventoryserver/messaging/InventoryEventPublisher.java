package com.example.inventoryserver.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public InventoryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishProductCreated(Long productId, String name) {
        ProductCreatedEvent event = new ProductCreatedEvent(productId, name);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "product.created", event);
    }

    public record ProductCreatedEvent(Long productId, String name) {}
}
