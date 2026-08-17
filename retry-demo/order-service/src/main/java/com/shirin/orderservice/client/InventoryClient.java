package com.shirin.orderservice.client;

import com.shirin.orderservice.dto.InventoryMode;
import com.shirin.orderservice.dto.ReserveInventoryRequest;
import com.shirin.orderservice.dto.ReserveInventoryResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final RestClient restClient;

    @Retry(name = "inventory-service")
    public ReserveInventoryResponse reserve(
            ReserveInventoryRequest request,
            InventoryMode mode) {

        log.debug("Calling Inventory Service for {}", request.productId());
        ReserveInventoryResponse response = restClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/reservations")
                        .queryParam("mode",mode)
                        .build())
                .body(request)
                .retrieve()
                .body(ReserveInventoryResponse.class);

        return response;
    }

}
