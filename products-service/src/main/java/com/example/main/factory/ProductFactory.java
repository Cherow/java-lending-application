package com.example.main.factory;

import com.example.main.model.dto.AddProductFeeRequest;
import com.example.main.model.dto.CreateProductRequest;
import com.example.main.model.dto.UpdateProductRequest;
import com.example.main.model.entity.Product;
import com.example.main.model.entity.ProductFee;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {

    public Product buildProduct(CreateProductRequest request) {
        return Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .tenureType(request.getTenureType())
                .minTenure(request.getMinTenure())
                .maxTenure(request.getMaxTenure())
                .fixedTenureAllowed(request.getFixedTenureAllowed())
                .flexibleTenureAllowed(request.getFlexibleTenureAllowed())
                .active(true)
                .build();
    }

    public void updateProduct(Product product, UpdateProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setTenureType(request.getTenureType());
        product.setMinTenure(request.getMinTenure());
        product.setMaxTenure(request.getMaxTenure());
        product.setFixedTenureAllowed(request.getFixedTenureAllowed());
        product.setFlexibleTenureAllowed(request.getFlexibleTenureAllowed());
        product.setActive(request.getActive());
    }

    public ProductFee buildProductFee(Product product, AddProductFeeRequest request) {
        return ProductFee.builder()
                .product(product)
                .feeName(request.getFeeName())
                .feeType(request.getFeeType())
                .calculationType(request.getCalculationType())
                .amount(resolveAmount(request))
                .percentage(resolvePercentage(request))
                .applicationStage(request.getApplicationStage())
                .daysAfterDue(request.getDaysAfterDue())
                .active(request.getActive())
                .build();
    }


    private java.math.BigDecimal resolveAmount(AddProductFeeRequest request) {
        return request.getCalculationType() == com.example.main.model.enums.CalculationType.FIXED
                ? request.getAmount()
                : null;
    }

    private java.math.BigDecimal resolvePercentage(AddProductFeeRequest request) {
        return request.getCalculationType() == com.example.main.model.enums.CalculationType.PERCENTAGE
                ? request.getPercentage()
                : null;
    }


}