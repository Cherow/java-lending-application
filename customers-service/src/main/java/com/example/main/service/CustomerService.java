package com.example.main.service;

import com.example.main.model.dto.*;
import com.example.main.model.entity.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request);
    CustomerResponse getCustomerById(Long customerId);
    CustomerLimitResponse setCustomerLimit(Long customerId, SetCustomerLimitRequest request);
    CustomerLimitResponse getCustomerLimit(Long customerId);
    Customer getCustomerEntity(Long customerId);
    void validateCustomerForLoan(Long customerId, BigDecimal requestedAmount);
    void consumeLimit(Long customerId, BigDecimal amount);
    void releaseLimit(Long customerId, BigDecimal amount);
}
