package com.shirin.outboxdemo.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirin.outboxdemo.contracts.EventTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class OutboxPublisher {

    private final OutboxClaimService claimService;
    private final OutboxStatusService statusService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String orderCreatedTopic;
    private final int maxRetries;
    private final int claimLimit;
    private final String owner;

    public OutboxPublisher(
            OutboxClaimService claimService,
            OutboxStatusService statusService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.order-created}") String orderCreatedTopic,
            @Value("${app.outbox.max-retries}") int maxRetries,
            @Value("${app.outbox.claim-limit}") int claimLimit
    ) {
        this.claimService = claimService;
        this.statusService = statusService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.orderCreatedTopic = orderCreatedTopic;
        this.maxRetries = maxRetries;
        this.claimLimit = claimLimit;
        this.owner = "order-service-" + UUID.randomUUID();
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms}")
    public void publishCandidateOutboxEvents() {
        List<OutboxEvent> events = claimService.claimOutboxEventsForPublish(claimLimit, owner);

        for (OutboxEvent event : events) {
            publish(event);
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }

    private void publish(OutboxEvent event) {
        try {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                    topicFor(event),
                    event.getAggregateId().toString(),
                    event.getEnvelopeWithPayload()
            );
            addEnvelopeHeaders(producerRecord, event.getEnvelopeWithPayload());

            kafkaTemplate
                    .send(producerRecord)
                    .get(5, TimeUnit.SECONDS);

            statusService.markPublished(event.getId() , owner);
            log.info("Outbox event published");

        } catch (InterruptedException ex) {
            try {
                statusService.markPublishFailed(event.getId(), errorMessage(ex), maxRetries, owner);

                log.warn("Outbox event publish failed", ex);

            } finally {
                Thread.currentThread().interrupt(); //restore interrupt flag
            }
        }catch (TimeoutException | ExecutionException | RuntimeException ex) {

            statusService.markPublishFailed( event.getId(), errorMessage(ex), maxRetries, owner);

            log.warn("Outbox event publish failed", ex);

        }
    }

    private void addEnvelopeHeaders(ProducerRecord<String, String> record, String envelopeWithPayload) {
        try {
            JsonNode root = objectMapper.readTree(envelopeWithPayload);

            addHeader(record, "event-id", root.path("eventId").asText());
            addHeader(record, "event-type", root.path("eventType").asText());
            addHeader(record, "event-version", root.path("eventVersion").asText());
            addHeader(record, "correlation-id", root.path("correlationId").asText());
            addHeader(record, "causation-id", root.path("causationId").asText());
            addHeader(record, "source", root.path("source").asText());

        }catch (Exception ex)
        {
            throw new InvalidOutboxEnvelopeException("Failed to extract envelope headers", ex);
        }
    }

    private void addHeader(ProducerRecord<String, String> record, String key, String value) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            return;
        }
        record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private String errorMessage(Exception ex) {

        Throwable target = ex.getCause() != null ? ex.getCause() : ex;
        String message = target.getMessage();

        return target.getClass().getSimpleName()
                + (message == null ? "" : ": " + message);

    }

    private String topicFor(OutboxEvent event) {
        if (EventTypes.ORDER_CREATED.equals(event.getEventType())) {
            return orderCreatedTopic;
        }
        throw new IllegalArgumentException("No topic configured for event type: " + event.getEventType());
    }
}
