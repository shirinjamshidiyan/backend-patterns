package com.shirin.outboxdemo.observability;

import org.slf4j.MDC;
import java.util.UUID;

public final class LoggingContext {

    private LoggingContext(){}

    public static void putRequestId(UUID requestId) {
        if(requestId!= null)
            MDC.put("requestId", requestId.toString());
    }

    public static void putOrderId(UUID orderId) {
        if(orderId != null )
            MDC.put("orderId", orderId.toString());
    }

    public static UUID currentCorrelationId() {
        String value = MDC.get("correlationId");

        if (value == null || value.isBlank()) {
            UUID generated = UUID.randomUUID();
            MDC.put("correlationId", generated.toString());
            return generated;
        }
        return UUID.fromString(value);
    }

    public static void clear() {
        MDC.clear();
    }
}