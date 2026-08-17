package com.shirin.orderservice.service;

import com.shirin.orderservice.client.InventoryClient;
import com.shirin.orderservice.dto.*;
import com.shirin.orderservice.exception.InventoryReservationRejectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final InventoryClient inventoryClient;

    public CreateOrderResponse createOrder(
            CreateOrderRequest request,
            InventoryMode inventoryMode) {

        log.info("Fetching inventory for productId={}", request.productId());

        ReserveInventoryResponse reservation = inventoryClient.reserve(
                new ReserveInventoryRequest(request.productId(), request.quantity()),
                inventoryMode
        );

        if (!"RESERVED".equals(reservation.status())) {
            throw new InventoryReservationRejectedException(
                    "Inventory reservation was not completed. status="
                            + reservation.status()
            );
        }

        return new CreateOrderResponse(
                "Order created successfully",
                request.productId(),
                request.quantity()
        );
    }

}
