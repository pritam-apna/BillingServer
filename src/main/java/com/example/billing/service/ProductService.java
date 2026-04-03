package com.example.billing.service;

import com.example.billing.dto.ProductDTO;
import com.example.billing.entity.Product;
import com.example.billing.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getAll() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> search(String query) {
        return productRepository.findByNameContainingIgnoreCase(query)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getPrice());
    }
}
