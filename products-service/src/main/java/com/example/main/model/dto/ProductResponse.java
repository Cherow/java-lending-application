package com.example.main.model.dto;

import com.example.main.model.enums.TenureType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private TenureType tenureType;
    private Integer minTenure;
    private Integer maxTenure;
    private Boolean active;
    private Boolean fixedTenureAllowed;
    private Boolean flexibleTenureAllowed;
    private List<ProductFeeResponse> fees;
}