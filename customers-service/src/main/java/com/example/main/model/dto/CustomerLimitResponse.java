package com.example.main.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CustomerLimitResponse {
    private Long id;
    private Long customerId;
    private BigDecimal maxLimit;
    private BigDecimal availableLimit;
    private BigDecimal utilizedLimit;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}