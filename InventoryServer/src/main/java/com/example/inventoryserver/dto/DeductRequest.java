package com.example.inventoryserver.dto;

public class DeductRequest {
    private String invoiceId;
    private java.util.List<Item> items;

    public static class Item {
        private Long productId;
        private Integer quantity;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public java.util.List<Item> getItems() { return items; }
    public void setItems(java.util.List<Item> items) { this.items = items; }
}
