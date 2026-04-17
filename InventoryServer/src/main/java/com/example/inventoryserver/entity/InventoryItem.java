package com.example.inventoryserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId; // Reference to BillingServer's Product ID

    private String itemName; // Display name for standalone use

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    public InventoryItem() {}

    public InventoryItem(Long productId, String itemName, Integer stockQuantity) {
        this.productId = productId;
        this.itemName = itemName;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
