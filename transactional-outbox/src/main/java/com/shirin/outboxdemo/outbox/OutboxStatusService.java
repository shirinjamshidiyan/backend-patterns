package com.shirin.outboxdemo.outbox;

import com.shirin.outboxdemo.observability.OutboxMetrics;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OutboxStatusService {

    private final OutboxEventRepository outboxRepository;
    private final OutboxMetrics metrics;

    @Transactional
    public void markPublished(UUID eventId, String owner) {

        int updated = outboxRepository.markPublishedByOwner(eventId, owner);

        if (updated == 1) {
            metrics.recordPublished();
        }
    }

    @Transactional
    public void markPublishFailed(UUID eventId, String error, int maxRetries, String owner) {

        int updated = outboxRepository.markFailedOrDeadByOwner(eventId, owner, error, maxRetries);

        if (updated == 1) {
            metrics.recordPublishFailed();
        }
    }

}
