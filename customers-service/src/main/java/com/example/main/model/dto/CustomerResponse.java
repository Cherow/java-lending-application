package com.example.main.model.dto;

import com.example.main.model.enums.CustomerStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String nationalId;
    private CustomerStatus status;
}