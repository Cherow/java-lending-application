package com.example.main.common;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerClient {

    private final WebClient webClient;

    @Value("${services.customer.base-url}")
    private String customerBaseUrl;

    public String getCustomerEntity(Long customerId) {
        String url = customerBaseUrl + "/api/customers/" + customerId;
        log.info("Getting customers by id {}", url);


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
            throw new EntityNotFoundException("Product not found with id: " + customerId);
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Error calling product service: " + ex.getResponseBodyAsString(), ex
            );
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error calling product service", ex);
        }

    }

    public void validateCustomerForLoan(Long customerId, BigDecimal amount) {
        String url = customerBaseUrl + "/api/customers/" + customerId + "/validate-loan";
        ValidateLoanRequest request = new ValidateLoanRequest(amount);

        try {
            webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (WebClientResponseException.Conflict ex) {
            throw new IllegalStateException("Customer is not eligible for loan");
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Customer validation failed: " + ex.getResponseBodyAsString(), ex
            );
        }
    }

    public void consumeLimit(Long customerId, BigDecimal amount) {
        try {
            webClient.post()
                    .uri(customerBaseUrl + "/api/customers/{id}/consume-limit", customerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new LimitRequest(amount))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Failed to consume customer limit: " + ex.getResponseBodyAsString(), ex);
        }
    }

    public void releaseLimit(Long customerId, BigDecimal amount) {
        try {
            webClient.post()
                    .uri(customerBaseUrl + "/api/customers/{id}/release-limit", customerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new LimitRequest(amount))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Failed to release customer limit: " + ex.getResponseBodyAsString(), ex);
        }
    }

    public record ValidateLoanRequest(BigDecimal amount) {}
    public record LimitRequest(BigDecimal amount) {}
}