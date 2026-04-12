package com.example.main.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomerRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    private String email;

    @NotBlank
    private String nationalId;
}