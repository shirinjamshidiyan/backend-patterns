package com.shirin.outboxdemo.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull UUID requestId,
        @NotNull UUID customerId,
        @NotNull @Positive BigDecimal totalAmount) {
}

