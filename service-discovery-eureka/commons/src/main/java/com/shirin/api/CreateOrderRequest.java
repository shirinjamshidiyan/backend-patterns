package com.shirin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull(message = "productId is required")
        @Positive(message = "productId must be greater than 0")
        Long productId,

        @NotBlank(message = "requestId is required")
        String requestId,

        @Positive(message = "quantity must be greater than 0")
        int quantity












) {
}
