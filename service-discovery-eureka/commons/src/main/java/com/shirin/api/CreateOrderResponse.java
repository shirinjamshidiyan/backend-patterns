package com.shirin.api;

public record CreateOrderResponse(

        String orderId,
        String status,
        String reservationId,
        String inventoryInstance
) {
}
