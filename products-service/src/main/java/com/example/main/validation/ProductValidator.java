package com.example.main.validation;

import com.example.main.model.dto.AddProductFeeRequest;
import com.example.main.model.enums.FeeType;
import org.springframework.stereotype.Component;

@Component
public class ProductValidator {

    public void validateTenure(Integer minTenure,
                               Integer maxTenure,
                               Boolean fixedAllowed,
                               Boolean flexibleAllowed) {

        if (minTenure == null || maxTenure == null) {
            throw new IllegalArgumentException("Min and Max tenure must not be null");
        }

        if (minTenure <= 0 || maxTenure <= 0) {
            throw new IllegalArgumentException("Tenure must be greater than zero");
        }

        if (minTenure > maxTenure) {
            throw new IllegalArgumentException("Minimum tenure cannot be greater than maximum tenure");
        }

        if (!Boolean.TRUE.equals(fixedAllowed) && !Boolean.TRUE.equals(flexibleAllowed)) {
            throw new IllegalArgumentException("At least one tenure mode (fixed or flexible) must be enabled");
        }
    }

    public void validateFee(AddProductFeeRequest request) {
        if (request.getCalculationType() == null) {
            throw new IllegalArgumentException("Calculation type is required");
        }

        if (request.getFeeType() == null) {
            throw new IllegalArgumentException("Fee type is required");
        }

        switch (request.getCalculationType()) {
            case FIXED -> {
                if (request.getAmount() == null) {
                    throw new IllegalArgumentException("Amount is required for FIXED fee");
                }
            }
            case PERCENTAGE -> {
                if (request.getPercentage() == null) {
                    throw new IllegalArgumentException("Percentage is required for PERCENTAGE fee");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported calculation type");
        }

        if (request.getFeeType() == FeeType.LATE && request.getDaysAfterDue() == null) {
            throw new IllegalArgumentException("daysAfterDue is required for late fees");
        }
    }
}