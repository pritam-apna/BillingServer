package com.example.inventoryserver.controller;

import com.example.inventoryserver.entity.InventoryItem;
import com.example.inventoryserver.repository.InventoryItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class DebugController {
    private final InventoryItemRepository repository;

    public DebugController(InventoryItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/debug/items")
    public List<InventoryItem> getItems() {
        return repository.findAll();
    }
}
