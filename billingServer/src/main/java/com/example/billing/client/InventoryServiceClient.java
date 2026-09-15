package com.example.billing.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceClient {

    private final WebClient webClient;
    private final String inventoryServerUrl;

    public InventoryServiceClient(WebClient inventoryWebClient, 
                                @Value("${inventory.server.url:http://localhost:8082}") String inventoryServerUrl) {
        this.webClient = inventoryWebClient;
        this.inventoryServerUrl = inventoryServerUrl;
    }

    public Mono<Void> deductStock(Long invoiceId, List<InventoryItem> items) {
        Map<String, Object> request = Map.of(
            "invoiceId", invoiceId,
            "items", items
        );

        return webClient.post()
                .uri(inventoryServerUrl + "/api/inventory/deduct")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public record InventoryItem(Long productId, Integer quantity) {}
}
