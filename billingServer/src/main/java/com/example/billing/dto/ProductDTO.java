package com.example.billing.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long taxCategoryId;
    private String taxCategoryName;
    private BigDecimal taxRate; // E.g., 0.10

    public ProductDTO() {}
    public ProductDTO(Long id, String name, BigDecimal price, Long taxCategoryId, String taxCategoryName, BigDecimal taxRate) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.taxCategoryId = taxCategoryId;
        this.taxCategoryName = taxCategoryName;
        this.taxRate = taxRate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Long getTaxCategoryId() { return taxCategoryId; }
    public void setTaxCategoryId(Long taxCategoryId) { this.taxCategoryId = taxCategoryId; }
    public String getTaxCategoryName() { return taxCategoryName; }
    public void setTaxCategoryName(String taxCategoryName) { this.taxCategoryName = taxCategoryName; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
}
