package com.example.main.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SetCustomerLimitRequest {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal maxLimit;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}