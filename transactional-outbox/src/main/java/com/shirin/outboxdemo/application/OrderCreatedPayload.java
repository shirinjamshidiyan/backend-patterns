package com.shirin.outboxdemo.application;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedPayload(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount) {

}
