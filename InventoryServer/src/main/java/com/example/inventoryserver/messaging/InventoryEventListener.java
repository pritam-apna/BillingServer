package com.example.inventoryserver.messaging;

import com.example.inventoryserver.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private final InventoryService inventoryService;

    @Value("${app.eventing.enabled:false}")
    private boolean eventingEnabled;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void listenForSale(SaleEvent event) {
        if (!eventingEnabled) {
            log.debug("Eventing is disabled. Ignoring Sale Event for invoice #{}", event.invoiceId());
            return;
        }
        log.info("Received Sale Event for invoice #{}", event.invoiceId());
        
        for (SaleItem item : event.items()) {
            inventoryService.adjustStock(
                item.productId(),
                -Math.abs(item.quantity()),
                "Invoice #" + event.invoiceId()
            );
        }
        log.info("Successfully processed stock adjustments for invoice #{}", event.invoiceId());
    }

    public record SaleEvent(Long invoiceId, List<SaleItem> items) {}
    public record SaleItem(Long productId, Integer quantity) {}
}
