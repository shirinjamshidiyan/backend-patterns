package com.shirin.api.Inventory;

public record   ReserveInventoryResponse(
        String requestId,
        String reservationId,
        String status,
        String processedBy
) {
}
