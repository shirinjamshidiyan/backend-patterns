package com.shirin.outboxdemo.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "envelope_with_payload" , columnDefinition = "TEXT", nullable = false)
    private String envelopeWithPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(name = "created_at", insertable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private String publishedBy;  //who publishes the event

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "processing_by")
    private String processingBy; //who currently claims the event

    protected OutboxEvent() {
    }
    private OutboxEvent(
            UUID id,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String envelope
    ) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.envelopeWithPayload = envelope;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public static OutboxEvent createPendingEvent(UUID id,String aggregateType, UUID aggregateId , String eventType, String payload) {
        return new OutboxEvent(id, aggregateType, aggregateId, eventType, payload);
    }

    public void markProcessing(String owner) {
        if (this.status == OutboxStatus.PENDING || this.status == OutboxStatus.FAILED) {
            this.status = OutboxStatus.PROCESSING;
            this.processingStartedAt = Instant.now();
            this.processingBy = owner;
        }
    }



}
