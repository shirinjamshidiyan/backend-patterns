package com.shirin.api.Inventory;

public record InventoryResponse(
        Long productId,
        int availableQuantity,
        boolean available,
        String processedBy
) {
}
