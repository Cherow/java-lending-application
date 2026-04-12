package com.example.main.service;


import com.example.main.model.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long productId, UpdateProductRequest request);
    ProductResponse getProductById(Long productId);
    List<ProductResponse> getAllProducts();
    ProductFeeResponse addFee(Long productId, AddProductFeeRequest request);
}