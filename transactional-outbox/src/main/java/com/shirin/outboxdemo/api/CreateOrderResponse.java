package com.shirin.outboxdemo.api;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        boolean duplicate) {
}
