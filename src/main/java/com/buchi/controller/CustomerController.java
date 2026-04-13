package com.buchi.controller;

import com.buchi.dto.request.AddCustomerRequest;
import com.buchi.dto.response.ApiResponse;
import com.buchi.entity.Customer;
import com.buchi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
        summary = "Add or retrieve a customer",
        description = "Creates a new customer. " +
                      "If the phone number already exists, returns the existing customer_id without creating a duplicate."
    )
    @PostMapping("/add_customer")
    public ResponseEntity<ApiResponse<Map<String, String>>> addCustomer(
            @RequestBody @Valid AddCustomerRequest request) {

        Customer customer = customerService.addOrGetCustomer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("customer_id", String.valueOf(customer.getId()))));
    }
}