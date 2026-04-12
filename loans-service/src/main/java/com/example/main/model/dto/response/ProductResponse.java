package com.example.main.model.dto.response;

import com.example.main.model.enums.TenureType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@Jacksonized
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