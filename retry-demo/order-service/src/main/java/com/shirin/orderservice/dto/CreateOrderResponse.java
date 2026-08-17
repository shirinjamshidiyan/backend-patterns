package com.shirin.orderservice.dto;

public record CreateOrderResponse(
        String message,
        String productId,
        int quantity
) {
}
