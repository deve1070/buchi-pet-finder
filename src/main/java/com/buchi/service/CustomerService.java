package com.buchi.service;

import com.buchi.dto.request.AddCustomerRequest;
import com.buchi.entity.Customer;
import com.buchi.exception.ResourceNotFoundException;
import com.buchi.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Upsert logic:
     * - If phone already exists → return existing customer (no duplicate)
     * - If phone is new → create and return new customer
     */
    @Transactional
    public Customer addOrGetCustomer(AddCustomerRequest request) {
        return customerRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    Customer customer = Customer.builder()
                            .name(request.getName())
                            .phone(request.getPhone())
                            .email(request.getEmail())
                            .build();
                    Customer saved = customerRepository.save(customer);
                    log.info("Created new customer id={} phone={}", saved.getId(), saved.getPhone());
                    return saved;
                });
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.customer(id));
    }
}