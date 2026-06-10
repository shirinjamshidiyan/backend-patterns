package com.shirin.outboxdemo.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
@Slf4j
public class OutboxRecoveryJob {

    private final OutboxEventRepository outboxRepository;
    private final long processingTimeoutSeconds;

    public OutboxRecoveryJob(
            OutboxEventRepository outboxRepository,
            @Value("${app.outbox.processing-timeout-seconds}") long processingTimeoutSeconds
    ) {
        this.outboxRepository = outboxRepository;
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }


    @Transactional
    @Scheduled(fixedDelayString = "${app.outbox.recovery-delay-ms}")
    public void recoverStuckProcessingEvents() {
        Instant threshold = Instant.now().minusSeconds(processingTimeoutSeconds);

        int reset = outboxRepository.resetStuckProcessingEvents(
                threshold,
                "Event was stuck in PROCESSING and was reset for retry"
        );
        if (reset > 0) {
            log.warn("Reset stuck PROCESSING outbox events: {}", reset);
        }

    }


}
