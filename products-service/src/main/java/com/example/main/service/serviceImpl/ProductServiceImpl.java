package com.example.main.service.serviceImpl;

import com.example.main.exception.BusinessException;
import com.example.main.exception.Utils;
import com.example.main.factory.ProductFactory;
import com.example.main.mapper.ProductMapper;
import com.example.main.model.dto.AddProductFeeRequest;
import com.example.main.model.dto.CreateProductRequest;
import com.example.main.model.dto.ProductFeeResponse;
import com.example.main.model.dto.ProductResponse;
import com.example.main.model.dto.UpdateProductRequest;
import com.example.main.model.entity.Product;
import com.example.main.model.entity.ProductFee;
import com.example.main.repository.ProductFeeRepository;
import com.example.main.repository.ProductRepository;
import com.example.main.service.ProductService;
import com.example.main.validation.ProductValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductFeeRepository productFeeRepository;
    private final ProductValidator productValidator;
    private final ProductFactory productFactory;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductFeeRepository productFeeRepository,
                              ProductValidator productValidator,
                              ProductFactory productFactory) {
        this.productRepository = productRepository;
        this.productFeeRepository = productFeeRepository;
        this.productValidator = productValidator;
        this.productFactory = productFactory;
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Received request to create product {}", Utils.setJsonString(request));

        productValidator.validateTenure(
                request.getMinTenure(),
                request.getMaxTenure(),
                request.getFixedTenureAllowed(),
                request.getFlexibleTenureAllowed()
        );

        if (productRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Product code already exists");
        }

        Product product = productFactory.buildProduct(request);
        Product savedProduct = productRepository.save(product);

        return ProductMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        productValidator.validateTenure(
                request.getMinTenure(),
                request.getMaxTenure(),
                request.getFixedTenureAllowed(),
                request.getFlexibleTenureAllowed()
        );

        Product product = getProductEntity(productId);
        productFactory.updateProduct(product, request);

        Product savedProduct = productRepository.save(product);
        return ProductMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        return ProductMapper.toResponse(getProductEntity(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public ProductFeeResponse addFee(Long productId, AddProductFeeRequest request) {
        Product product = getProductEntity(productId);
        productValidator.validateFee(request);

        ProductFee fee = productFactory.buildProductFee(product, request);
        ProductFee savedFee = productFeeRepository.save(fee);

        return ProductMapper.toFeeResponse(savedFee);
    }

    private Product getProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }
}