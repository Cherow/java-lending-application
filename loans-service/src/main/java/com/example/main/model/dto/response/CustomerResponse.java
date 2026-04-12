package com.example.main.model.dto.response;


import com.example.main.model.enums.CustomerStatus;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
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