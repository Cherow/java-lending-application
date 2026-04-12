package com.example.main.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class CreateLoanRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal principalAmount;

    @NotNull
    @Min(1)
    private Integer tenure;
}