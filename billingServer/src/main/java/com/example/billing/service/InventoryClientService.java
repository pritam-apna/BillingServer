package com.example.billing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class InventoryClientService {

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public InventoryClientService(
            @Value("${inventory.server.url:http://localhost:8082}") String inventoryUrl,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        this.restClient = RestClient.builder().baseUrl(inventoryUrl).build();
        this.authorizedClientManager = authorizedClientManager;
    }

    /**
     * Fetches a fresh OAuth2 Client Credentials token from the AuthServer
     * on behalf of the "billing-service" registered client.
     */
    private String getBearerToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
            .withClientRegistrationId("billing-service")
            .principal("billing-service")
            .build();
        var client = authorizedClientManager.authorize(request);
        if (client == null) {
            throw new IllegalStateException("Could not obtain OAuth2 token for billing-service");
        }
        OAuth2AccessToken token = client.getAccessToken();
        return "Bearer " + token.getTokenValue();
    }

    @SuppressWarnings("unchecked")
    public Integer getStock(Long productId) {
        try {
            Map<String, Integer> response = restClient.get()
                .uri("/api/inventory/{productId}", productId)
                .header("Authorization", getBearerToken())
                .retrieve()
                .body(Map.class);
            return response != null ? response.getOrDefault("stockQuantity", 0) : 0;
        } catch (Exception e) {
            System.err.println("Failed to fetch stock for Product " + productId + ": " + e.getMessage());
            return 0;
        }
    }

    public void deductStock(String invoiceId, List<Map<String, Object>> items) {
        Map<String, Object> request = Map.of(
            "invoiceId", invoiceId,
            "items", items
        );
        restClient.post()
            .uri("/api/inventory/deduct")
            .header("Authorization", getBearerToken())
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}
