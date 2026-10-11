package com.shirin.orderservice.service;

import com.shirin.api.Inventory.InventoryResponse;
import com.shirin.api.Inventory.ReserveInventoryRequest;
import com.shirin.api.Inventory.ReserveInventoryResponse;
import com.shirin.api.order.CreateOrderRequest;
import com.shirin.api.order.CreateOrderResponse;
import com.shirin.orderservice.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final InventoryClient inventoryClient;


    public CreateOrderResponse createOrder(CreateOrderRequest request) {

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

    public InventoryResponse checkInventory(Long productId) {
        return inventoryClient.getInventory(productId);
    }

}
