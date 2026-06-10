package com.shirin.outboxdemo.application;

import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        boolean duplicate) {
}
