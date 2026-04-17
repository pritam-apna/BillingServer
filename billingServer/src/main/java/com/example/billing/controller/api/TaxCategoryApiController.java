package com.example.billing.controller.api;

import com.example.billing.entity.TaxCategory;
import com.example.billing.repository.TaxCategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tax-categories")
public class TaxCategoryApiController {

    private final TaxCategoryRepository repository;

    public TaxCategoryApiController(TaxCategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TaxCategory> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public TaxCategory create(@RequestBody TaxCategory category) {
        return repository.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxCategory> update(@PathVariable Long id, @RequestBody TaxCategory category) {
        return repository.findById(id).map(existing -> {
            existing.setName(category.getName());
            existing.setTaxRate(category.getTaxRate());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
