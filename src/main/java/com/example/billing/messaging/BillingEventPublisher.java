package com.example.billing.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BillingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public BillingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSale(Long invoiceId, List<SaleItem> items) {
        SaleEvent event = new SaleEvent(invoiceId, items);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "sale.event", event);
    }

    public record SaleEvent(Long invoiceId, List<SaleItem> items) {}
    public record SaleItem(Long productId, Integer quantity) {}
}
