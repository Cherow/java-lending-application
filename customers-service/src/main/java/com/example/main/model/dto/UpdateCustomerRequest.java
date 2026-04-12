package com.example.main.model.dto;

import com.example.main.model.enums.CustomerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    private String email;

    @NotNull
    private CustomerStatus status;
}