package com.shirin.orderservice.service;

import com.shirin.api.*;
import com.shirin.orderservice.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final InventoryClient inventoryClient;

    public InventoryResponse checkInventory(Long productId) {
        return inventoryClient.getInventory(productId);
    }


    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        log.info("Fetching inventory for productId={}", request.productId());

        ReserveInventoryResponse reservation = inventoryClient.reserve(
                new ReserveInventoryRequest(
                        request.requestId(),
                        request.productId(),
                        request.quantity()));

        return new CreateOrderResponse(
                "ord-" + UUID.randomUUID(),
                "CREATED",
                reservation.reservationId(),
                reservation.processedBy()
        );
    }

}
