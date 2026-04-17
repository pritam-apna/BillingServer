package com.example.inventoryserver.controller;

import com.example.inventoryserver.dto.AdjustRequest;
import com.example.inventoryserver.dto.DeductRequest;
import com.example.inventoryserver.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*") // For local UI integrations if needed
public class InventoryApiController {

    private final InventoryService inventoryService;

    public InventoryApiController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Integer>> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("stockQuantity", inventoryService.getStock(productId)));
    }

    @PostMapping("/adjust")
    public ResponseEntity<Void> adjustStock(@RequestBody AdjustRequest request) {
        inventoryService.adjustStock(request.getProductId(), request.getQuantityChange(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deductStock(@RequestBody DeductRequest request) {
        // Validate stock beforehand if required... For this MVP, we proceed directly
        for (DeductRequest.Item item : request.getItems()) {
            inventoryService.adjustStock(
                item.getProductId(), 
                -Math.abs(item.getQuantity()), // ensure deduction
                "Invoice #" + request.getInvoiceId()
            );
        }
        return ResponseEntity.ok().build();
    }
}
