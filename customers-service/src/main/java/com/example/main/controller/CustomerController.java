package com.example.main.controller;

import com.example.main.model.dto.*;
import com.example.main.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return new ResponseEntity<>(customerService.createCustomer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @PostMapping("/{customerId}/limit")
    public ResponseEntity<CustomerLimitResponse> setCustomerLimit(
            @PathVariable Long customerId,
            @Valid @RequestBody SetCustomerLimitRequest request) {
        return ResponseEntity.ok(customerService.setCustomerLimit(customerId, request));
    }

    @GetMapping("/{customerId}/limit")
    public ResponseEntity<CustomerLimitResponse> getCustomerLimit(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerLimit(customerId));
    }

    @PostMapping("/{customerId}/validate-loan")
    public ResponseEntity<Void> validateCustomerForLoan(
            @PathVariable Long customerId,
            @RequestBody LoanValidationRequest request) {

        log.info("Received validate-loan request for customerId={}, requestedAmount={}",
                customerId, request.getRequestedAmount());

        customerService.validateCustomerForLoan(customerId, request.getRequestedAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{customerId}/consume-limit")
    public ResponseEntity<Void> consumeLimit(
            @PathVariable Long customerId,
            @RequestBody LimitUpdateRequest request) {
        customerService.consumeLimit(customerId, request.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{customerId}/release-limit")
    public ResponseEntity<Void> releaseLimit(
            @PathVariable Long customerId,
            @RequestBody LimitUpdateRequest request) {
        customerService.releaseLimit(customerId, request.getAmount());
        return ResponseEntity.ok().build();
    }
}