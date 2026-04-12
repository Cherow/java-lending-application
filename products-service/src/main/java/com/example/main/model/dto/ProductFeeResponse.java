package com.example.main.model.dto;

import com.example.main.model.enums.ApplicationStage;
import com.example.main.model.enums.CalculationType;
import com.example.main.model.enums.FeeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductFeeResponse {
    private Long id;
    private String feeName;
    private FeeType feeType;
    private CalculationType calculationType;
    private BigDecimal amount;
    private BigDecimal percentage;
    private ApplicationStage applicationStage;
    private Integer daysAfterDue;
    private Boolean active;
}