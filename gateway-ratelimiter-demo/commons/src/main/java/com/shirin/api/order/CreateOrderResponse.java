package com.shirin.api.order;

public record CreateOrderResponse(

        String orderId,
        String status,
        String reservationId,
        String inventoryInstance
) {
}
