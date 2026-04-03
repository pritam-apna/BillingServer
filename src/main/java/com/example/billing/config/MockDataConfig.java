package com.example.billing.config;

import com.example.billing.entity.Customer;
import com.example.billing.entity.Product;
import com.example.billing.repository.CustomerRepository;
import com.example.billing.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class MockDataConfig {

    @Bean
    public CommandLineRunner initData(CustomerRepository customerRepository, ProductRepository productRepository) {
        return args -> {
            if (customerRepository.count() == 0) {
                customerRepository.saveAll(List.of(
                        new Customer("John Doe", "555-0100"),
                        new Customer("Jane Smith", "555-0101"),
                        new Customer("Acme Corp", "555-0102")
                ));
            }

            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new Product("Laptop Pro", new BigDecimal("1299.99")),
                        new Product("Wireless Mouse", new BigDecimal("49.99")),
                        new Product("Ergonomic Keyboard", new BigDecimal("89.99")),
                        new Product("USB-C Hub", new BigDecimal("39.99")),
                        new Product("HD Monitor 27\"", new BigDecimal("249.99"))
                ));
            }
        };
    }
}
