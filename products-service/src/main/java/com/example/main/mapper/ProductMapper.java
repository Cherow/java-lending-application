package com.example.main.mapper;

import com.example.main.model.dto.ProductFeeResponse;
import com.example.main.model.dto.ProductResponse;
import com.example.main.model.entity.Product;
import com.example.main.model.entity.ProductFee;

import java.util.Optional;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .tenureType(product.getTenureType())
                .minTenure(product.getMinTenure())
                .maxTenure(product.getMaxTenure())
                .active(product.getActive())
                .fixedTenureAllowed(product.getFixedTenureAllowed())
                .flexibleTenureAllowed(product.getFlexibleTenureAllowed())
                .fees(Optional.ofNullable(product.getFees())
                        .map(fees -> fees.stream()
                                .map(ProductMapper::toFeeResponse)
                                .collect(Collectors.toList()))
                        .orElse(null))
                .build();
    }

    public static ProductFeeResponse toFeeResponse(ProductFee fee) {
        return ProductFeeResponse.builder()
                .id(fee.getId())
                .feeName(fee.getFeeName())
                .feeType(fee.getFeeType())
                .calculationType(fee.getCalculationType())
                .amount(fee.getAmount())
                .percentage(fee.getPercentage())
                .applicationStage(fee.getApplicationStage())
                .daysAfterDue(fee.getDaysAfterDue())
                .active(fee.getActive())
                .build();
    }
}