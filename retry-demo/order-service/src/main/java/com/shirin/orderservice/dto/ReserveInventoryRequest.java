package com.shirin.orderservice.dto;

public record ReserveInventoryRequest(
        String productId,
        int quantity
) {
}
