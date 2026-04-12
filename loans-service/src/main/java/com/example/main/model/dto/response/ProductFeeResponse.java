package com.example.main.model.dto.response;

import com.example.main.model.enums.ApplicationStage;
import com.example.main.model.enums.CalculationType;
import com.example.main.model.enums.FeeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
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