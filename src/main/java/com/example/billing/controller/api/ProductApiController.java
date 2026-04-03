package com.example.billing.controller.api;

import com.example.billing.dto.ProductDTO;
import com.example.billing.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAll();
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(@RequestParam String q) {
        return productService.search(q);
    }
}
