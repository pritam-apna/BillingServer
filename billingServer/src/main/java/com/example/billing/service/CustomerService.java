package com.example.billing.service;

import com.example.billing.dto.CustomerDTO;
import com.example.billing.entity.Customer;
import com.example.billing.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerDTO> getAll() {
        return customerRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CustomerDTO> search(String query) {
        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query, query)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public CustomerDTO create(CustomerDTO dto) {
        Customer customer = new Customer(dto.getName(), dto.getPhone(), dto.getEmail());
        customer = customerRepository.save(customer);
        return mapToDTO(customer);
    }

    private CustomerDTO mapToDTO(Customer customer) {
        return new CustomerDTO(customer.getId(), customer.getName(), customer.getPhone(), customer.getEmail());
    }
}
