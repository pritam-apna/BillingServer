package com.example.billing.service;

import com.example.billing.dto.ProductDTO;
import com.example.billing.entity.Product;
import com.example.billing.entity.TaxCategory;
import com.example.billing.repository.ProductRepository;
import com.example.billing.repository.TaxCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final TaxCategoryRepository taxCategoryRepository;

    public ProductService(ProductRepository productRepository, TaxCategoryRepository taxCategoryRepository) {
        this.productRepository = productRepository;
        this.taxCategoryRepository = taxCategoryRepository;
    }

    public List<ProductDTO> getAll() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> search(String query) {
        return productRepository.findByNameContainingIgnoreCase(query)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductDTO create(ProductDTO dto) {
        TaxCategory cat = null;
        if (dto.getTaxCategoryId() != null) {
            cat = taxCategoryRepository.findById(dto.getTaxCategoryId()).orElse(null);
        }
        Product p = new Product(dto.getName(), dto.getPrice(), cat);
        return mapToDTO(productRepository.save(p));
    }

    public ProductDTO update(Long id, ProductDTO dto) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        p.setName(dto.getName());
        p.setPrice(dto.getPrice());
        if (dto.getTaxCategoryId() != null) {
            p.setCategory(taxCategoryRepository.findById(dto.getTaxCategoryId()).orElse(null));
        } else {
            p.setCategory(null);
        }
        return mapToDTO(productRepository.save(p));
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    private ProductDTO mapToDTO(Product product) {
        Long catId = null;
        String catName = null;
        java.math.BigDecimal taxRate = null;
        if(product.getCategory() != null) {
            catId = product.getCategory().getId();
            catName = product.getCategory().getName();
            taxRate = product.getCategory().getTaxRate();
        }
        return new ProductDTO(product.getId(), product.getName(), product.getPrice(), catId, catName, taxRate);
    }
}
