package com.example.main.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanValidationRequest {
    private BigDecimal requestedAmount;
}