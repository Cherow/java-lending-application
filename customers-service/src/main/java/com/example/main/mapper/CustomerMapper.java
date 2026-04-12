package com.example.main.mapper;

import com.example.main.model.dto.CustomerLimitResponse;
import com.example.main.model.dto.CustomerResponse;
import com.example.main.model.entity.Customer;
import com.example.main.model.entity.CustomerLimit;

public class CustomerMapper {

    public static CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerNumber(customer.getCustomerNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .nationalId(customer.getNationalId())
                .status(customer.getStatus())
                .build();
    }

    public static CustomerLimitResponse toLimitResponse(CustomerLimit limit) {
        return CustomerLimitResponse.builder()
                .id(limit.getId())
                .customerId(limit.getCustomerId())
                .maxLimit(limit.getMaxLimit())
                .availableLimit(limit.getAvailableLimit())
                .utilizedLimit(limit.getUtilizedLimit())
                .effectiveFrom(limit.getEffectiveFrom())
                .effectiveTo(limit.getEffectiveTo())
                .build();
    }
}