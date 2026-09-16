package com.shirin.api;

public record InventoryResponse(
        Long productId,
        int availableQuantity,
        boolean available,
        String processedBy
) {
}
