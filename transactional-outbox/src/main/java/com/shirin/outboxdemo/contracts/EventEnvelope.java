package com.shirin.outboxdemo.contracts;

import java.time.Instant;
import java.util.UUID;

/*
Contains metadata for tracking, tracing, debugging, replay and observability.
 */
public record EventEnvelope<T> (
        UUID eventId,
        String eventType,
        int eventVersion,
        UUID correlationId,
        UUID causationId,
        Instant occurredAt,
        String source,
        T payload
){
}
