package com.example.main.gateway;

import com.example.main.common.ProductClient;
import com.example.main.common.Utils;
import com.example.main.model.dto.response.ProductResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductGateway {

    private final ProductClient productClient;

    public ProductResponse getProductById(Long productId) {
        String response = productClient.getProductById(productId);
        ProductResponse product = (ProductResponse) Utils.setJsonStringToObject(response, ProductResponse.class);

        if (product == null) {
            throw new EntityNotFoundException("Product not found");
        }

        return product;
    }
}