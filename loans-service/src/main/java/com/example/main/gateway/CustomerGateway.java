package com.example.main.gateway;

import com.example.main.common.CustomerClient;
import com.example.main.common.Utils;
import com.example.main.model.dto.response.CustomerResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGateway {

    private final CustomerClient customerClient;

    public CustomerResponse getCustomerById(Long customerId) {
        String response = customerClient.getCustomerEntity(customerId);
        CustomerResponse customer = (CustomerResponse) Utils.setJsonStringToObject(response, CustomerResponse.class);

        if (customer == null) {
            throw new EntityNotFoundException("Customer not found");
        }

        return customer;
    }

    public void validateCustomerForLoan(Long customerId, java.math.BigDecimal amount) {
        customerClient.validateCustomerForLoan(customerId, amount);
    }

    public void consumeLimit(Long customerId, java.math.BigDecimal amount) {
        customerClient.consumeLimit(customerId, amount);
    }

    public void releaseLimit(Long customerId, java.math.BigDecimal amount) {
        customerClient.releaseLimit(customerId, amount);
    }
}