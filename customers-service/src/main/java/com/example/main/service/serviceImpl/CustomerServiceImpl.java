package com.example.main.service.serviceImpl;

import com.example.main.common.Utils;
import com.example.main.mapper.CustomerMapper;
import com.example.main.model.dto.*;
import com.example.main.model.entity.Customer;
import com.example.main.model.entity.CustomerLimit;
import com.example.main.model.enums.CustomerStatus;
import com.example.main.repository.CustomerLimitRepository;
import com.example.main.repository.CustomerRepository;
import com.example.main.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerLimitRepository customerLimitRepository;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Received request to create product {}", Utils.setJsonString(request));

        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new IllegalArgumentException("National ID already exists");
        }

        Customer customer = Customer.builder()
                .customerNumber(generateCustomerNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .nationalId(request.getNationalId())
                .status(CustomerStatus.ACTIVE)
                .build();

        return CustomerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer customer = getCustomerEntity(customerId);

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setStatus(request.getStatus());

        return CustomerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        return CustomerMapper.toResponse(getCustomerEntity(customerId));
    }

    @Override
    public CustomerLimitResponse setCustomerLimit(Long customerId, SetCustomerLimitRequest request) {
        Customer customer = getCustomerEntity(customerId);

        CustomerLimit limit = customerLimitRepository.findByCustomerId(customer.getId())
                .orElse(
                        CustomerLimit.builder()
                                .customerId(customer.getId())
                                .utilizedLimit(BigDecimal.ZERO)
                                .build()
                );

        limit.setMaxLimit(request.getMaxLimit());
        limit.setAvailableLimit(request.getMaxLimit().subtract(limit.getUtilizedLimit()));
        limit.setEffectiveFrom(request.getEffectiveFrom());
        limit.setEffectiveTo(request.getEffectiveTo());

        return CustomerMapper.toLimitResponse(customerLimitRepository.save(limit));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerLimitResponse getCustomerLimit(Long customerId) {
        CustomerLimit limit = customerLimitRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer limit not found"));

        return CustomerMapper.toLimitResponse(limit);
    }

    @Override
    public void validateCustomerForLoan(Long customerId, BigDecimal requestedAmount) {
        Customer customer = getCustomerEntity(customerId);

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer is not eligible for borrowing");
        }

        CustomerLimit limit = customerLimitRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer limit not found"));

        if (limit.getAvailableLimit().compareTo(requestedAmount) < 0) {
            throw new IllegalArgumentException("Insufficient available limit");
        }
    }



    @Override
    public void consumeLimit(Long customerId, BigDecimal amount) {
        CustomerLimit limit = customerLimitRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer limit not found"));

        if (limit.getAvailableLimit().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available limit");
        }

        limit.setAvailableLimit(limit.getAvailableLimit().subtract(amount));
        limit.setUtilizedLimit(limit.getUtilizedLimit().add(amount));

        customerLimitRepository.save(limit);
    }

    @Override
    public void releaseLimit(Long customerId, BigDecimal amount) {
        CustomerLimit limit = customerLimitRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer limit not found"));

        limit.setUtilizedLimit(limit.getUtilizedLimit().subtract(amount));
        limit.setAvailableLimit(limit.getAvailableLimit().add(amount));

        customerLimitRepository.save(limit);
    }

     public Customer getCustomerEntity(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    private String generateCustomerNumber() {
        return "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}