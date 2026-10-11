package com.shirin.api.Inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveInventoryRequest(
        @NotBlank
        @NotBlank
        String requestId,
        @NotNull
        @Positive
        Long productId,
        @Positive
        int quantity
) {
}
