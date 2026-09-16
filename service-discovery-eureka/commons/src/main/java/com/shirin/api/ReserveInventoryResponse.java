package com.shirin.api;

public record ReserveInventoryResponse(
        String requestId,
        String reservationId,
        String status,
        String processedBy
) {
}
