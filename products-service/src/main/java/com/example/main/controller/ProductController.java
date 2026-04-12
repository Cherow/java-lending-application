package com.example.main.controller;


import com.example.main.model.dto.*;
import com.example.main.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    @PostMapping("/{productId}/fees")
    public ResponseEntity<ProductFeeResponse> addFee(
            @PathVariable Long productId,
            @Valid @RequestBody AddProductFeeRequest request) {
        return new ResponseEntity<>(productService.addFee(productId, request), HttpStatus.CREATED);
    }
}