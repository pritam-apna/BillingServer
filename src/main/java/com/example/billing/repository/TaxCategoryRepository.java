package com.example.billing.repository;

import com.example.billing.entity.TaxCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxCategoryRepository extends JpaRepository<TaxCategory, Long> {
}
