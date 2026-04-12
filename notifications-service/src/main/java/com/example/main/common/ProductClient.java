package com.example.main.common;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient webClient;

    @Value("${services.product.base-url}")
    private String productBaseUrl;

    public String getProductById(Long productId) {
        String url = productBaseUrl + "/api/products/" + productId;
        log.info("Getting product by id {}", url);

        try {
            ResponseEntity<String> response = webClient
                    .method(HttpMethod.GET)
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response == null || response.getBody() == null) {
                throw new RuntimeException("Empty response from product service");
            }

            log.info("Product service response {}", response.getBody());
            return response.getBody();

        } catch (WebClientResponseException.NotFound ex) {
            throw new EntityNotFoundException("Product not found with id: " + productId);
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Error calling product service: " + ex.getResponseBodyAsString(), ex
            );
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error calling product service", ex);
        }
    }
}