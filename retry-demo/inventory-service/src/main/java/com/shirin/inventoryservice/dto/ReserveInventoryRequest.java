package com.shirin.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveInventoryRequest(
        @NotBlank String productId,
        @Min(1) int quantity
) {
}
