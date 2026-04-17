package com.example.inventoryserver.service;

import com.example.inventoryserver.entity.InventoryItem;
import com.example.inventoryserver.entity.InventoryTransaction;
import com.example.inventoryserver.repository.InventoryItemRepository;
import com.example.inventoryserver.repository.InventoryTransactionRepository;
import com.example.inventoryserver.messaging.InventoryEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class InventoryService {
    
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryEventPublisher inventoryEventPublisher;

    public InventoryService(InventoryItemRepository itemRepo, 
                            InventoryTransactionRepository transRepo,
                            InventoryEventPublisher inventoryEventPublisher) {
        this.inventoryItemRepository = itemRepo;
        this.inventoryTransactionRepository = transRepo;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    public Integer getStock(Long productId) {
        return inventoryItemRepository.findByProductId(productId)
            .map(InventoryItem::getStockQuantity)
            .orElse(0); // If never tracked, assume 0
    }

    @Transactional
    public void adjustStock(Long productId, String itemName, Integer change, String reference) {
        boolean isNew = false;
        InventoryItem item = inventoryItemRepository.findByProductId(productId).orElse(null);
        
        if (item == null) {
            item = new InventoryItem(productId, itemName, 0);
            isNew = true;
        }
        
        if (itemName != null && !itemName.isEmpty()) {
            item.setItemName(itemName);
        }
        
        item.setStockQuantity(item.getStockQuantity() + change);
        inventoryItemRepository.save(item);

        InventoryTransaction tx = new InventoryTransaction(productId, change, reference, LocalDateTime.now());
        inventoryTransactionRepository.save(tx);

        if (isNew && itemName != null && !itemName.isEmpty()) {
            inventoryEventPublisher.publishProductCreated(productId, itemName);
        }
    }

    // Overload for cases where name is unknown (e.g. deductions)
    @Transactional
    public void adjustStock(Long productId, Integer change, String reference) {
        adjustStock(productId, null, change, reference);
    }

    public java.util.List<InventoryItem> findAllItems() {
        return inventoryItemRepository.findAll();
    }

    public java.util.List<InventoryTransaction> findRecentTransactions() {
        return inventoryTransactionRepository.findTop10ByOrderByTimestampDesc();
    }
}
