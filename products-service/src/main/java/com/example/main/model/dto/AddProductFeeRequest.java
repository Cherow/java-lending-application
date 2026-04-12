package com.example.main.model.dto;


import com.example.main.model.enums.ApplicationStage;
import com.example.main.model.enums.CalculationType;
import com.example.main.model.enums.FeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductFeeRequest {

    @NotBlank
    private String feeName;

    @NotNull
    private FeeType feeType;

    @NotNull
    private CalculationType calculationType;

    private BigDecimal amount;

    private BigDecimal percentage;

    @NotNull
    private ApplicationStage applicationStage;

    private Integer daysAfterDue;

    @NotNull
    private Boolean active;
}