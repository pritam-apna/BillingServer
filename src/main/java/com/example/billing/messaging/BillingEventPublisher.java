package com.example.billing.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BillingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.eventing.enabled:false}")
    private boolean eventingEnabled;

    public BillingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSale(Long invoiceId, List<SaleItem> items) {
        if (!eventingEnabled) {
            log.debug("Eventing is disabled. Skipping sale event publication for invoice #{}", invoiceId);
            return;
        }
        log.info("Publishing Sale Event for invoice #{}", invoiceId);
        SaleEvent event = new SaleEvent(invoiceId, items);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "sale.event", event);
        log.debug("Billing event is sent{}",event);
    }

    public record SaleEvent(Long invoiceId, List<SaleItem> items) {}
    public record SaleItem(Long productId, Integer quantity) {}
}
