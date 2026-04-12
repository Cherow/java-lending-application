package com.example.main.model.dto;

import com.example.main.model.enums.TenureType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProductRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private TenureType tenureType;

    @NotNull
    @Min(1)
    private Integer minTenure;

    @NotNull
    @Min(1)
    private Integer maxTenure;

    @NotNull
    private Boolean fixedTenureAllowed;

    @NotNull
    private Boolean flexibleTenureAllowed;

    @NotNull
    private Boolean active;
}