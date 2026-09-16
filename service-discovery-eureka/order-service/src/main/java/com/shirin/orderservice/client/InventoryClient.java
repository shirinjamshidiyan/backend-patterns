package com.shirin.orderservice.client;

import com.shirin.api.InventoryResponse;
import com.shirin.api.ReserveInventoryRequest;
import com.shirin.api.ReserveInventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            @LoadBalanced RestClient.Builder restClientBuiler,
            @Value("${clients.inventory.baseUrl}") String baseUrl){

        this.restClient = restClientBuiler
                .baseUrl(baseUrl)
                .build();
    }

    public ReserveInventoryResponse reserve(ReserveInventoryRequest request) {

        log.debug("Calling Inventory Service for {}", request.productId());
        return restClient
                .post()
                .uri("/reservations")
                .body(request)
                .retrieve()
                .body(ReserveInventoryResponse.class);

    }

    public InventoryResponse getInventory(Long productId) {
        return restClient.get()
                .uri("/{productId}", productId)
                .retrieve()
                .body(InventoryResponse.class);
    }

}
