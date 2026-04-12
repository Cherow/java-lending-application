package com.example.main.serviceImpl;

import com.example.main.exception.BusinessException;
import com.example.main.exception.Utils;
import com.example.main.mapper.ProductMapper;
import com.example.main.model.dto.*;
import com.example.main.model.entity.Product;
import com.example.main.model.entity.ProductFee;
import com.example.main.repository.ProductFeeRepository;
import com.example.main.repository.ProductRepository;
import com.example.main.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductFeeRepository productFeeRepository;

    public ProductServiceImpl(ProductRepository productRepository, ProductFeeRepository productFeeRepository) {
        this.productRepository = productRepository;
        this.productFeeRepository = productFeeRepository;
    }


    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Received request to create product {}", Utils.setJsonString(request));
        validateTenure(request.getMinTenure(), request.getMaxTenure(), request.getFixedTenureAllowed(), request.getFlexibleTenureAllowed());

        if (productRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Product code already exists");
        }

        Product product = Product.builder()
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

        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        validateTenure(request.getMinTenure(), request.getMaxTenure(), request.getFixedTenureAllowed(), request.getFlexibleTenureAllowed());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setTenureType(request.getTenureType());
        product.setMinTenure(request.getMinTenure());
        product.setMaxTenure(request.getMaxTenure());
        product.setFixedTenureAllowed(request.getFixedTenureAllowed());
        product.setFlexibleTenureAllowed(request.getFlexibleTenureAllowed());
        product.setActive(request.getActive());

        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public ProductFeeResponse addFee(Long productId, AddProductFeeRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        validateFee(request);

        ProductFee fee = ProductFee.builder()
                .product(product)
                .feeName(request.getFeeName())
                .feeType(request.getFeeType())
                .calculationType(request.getCalculationType())
                .amount(request.getAmount())
                .percentage(request.getPercentage())
                .applicationStage(request.getApplicationStage())
                .daysAfterDue(request.getDaysAfterDue())
                .active(request.getActive())
                .build();

        return ProductMapper.toFeeResponse(productFeeRepository.save(fee));
    }



    private void validateTenure(Integer minTenure,
                                Integer maxTenure,
                                Boolean fixedAllowed,
                                Boolean flexibleAllowed) {

        // 1. Null checks
        if (minTenure == null || maxTenure == null) {
            throw new IllegalArgumentException("Min and Max tenure must not be null");
        }

        // 2. Positive values check
        if (minTenure <= 0 || maxTenure <= 0) {
            throw new IllegalArgumentException("Tenure must be greater than zero");
        }

        // 3. Logical validation
        if (minTenure > maxTenure) {
            throw new IllegalArgumentException("Minimum tenure cannot be greater than maximum tenure");
        }

        // 4. Business rule: at least one must be allowed
        if (!Boolean.TRUE.equals(fixedAllowed) && !Boolean.TRUE.equals(flexibleAllowed)) {
            throw new IllegalArgumentException("At least one tenure mode (fixed or flexible) must be enabled");
        }
    }


    private void validateFee(AddProductFeeRequest request) {
        switch (request.getCalculationType()) {
            case FIXED -> {
                if (request.getAmount() == null) {
                    throw new IllegalArgumentException("Amount is required for FIXED fee");
                }
                request.setPercentage(null);
            }
            case PERCENTAGE -> {
                if (request.getPercentage() == null) {
                    throw new IllegalArgumentException("Percentage is required for PERCENTAGE fee");
                }
                request.setAmount(null);
            }
        }

        if (request.getFeeType().name().equals("LATE") && request.getDaysAfterDue() == null) {
            throw new IllegalArgumentException("daysAfterDue is required for late fees");
        }
    }


}

