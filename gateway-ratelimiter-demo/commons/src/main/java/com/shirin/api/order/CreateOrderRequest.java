package com.shirin.api.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull
        @Positive
        Long productId,

        @NotBlank
        String requestId,

        @Positive
        int quantity












) {
}
