package com.shirin.outboxdemo.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderCommand(
        UUID requestId,
        UUID correlationId,
        UUID customerId,
        BigDecimal totalAmount) {
}
