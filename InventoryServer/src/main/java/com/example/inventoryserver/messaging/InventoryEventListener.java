package com.example.inventoryserver.messaging;

import com.example.inventoryserver.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryEventListener {

    private final InventoryService inventoryService;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void listenForSale(SaleEvent event) {
        System.out.println("Received Sale Event: " + event.invoiceId());
        
        for (SaleItem item : event.items()) {
            inventoryService.adjustStock(
                item.productId(),
                -Math.abs(item.quantity()),
                "Invoice #" + event.invoiceId()
            );
        }
    }

    public record SaleEvent(Long invoiceId, List<SaleItem> items) {}
    public record SaleItem(Long productId, Integer quantity) {}
}
