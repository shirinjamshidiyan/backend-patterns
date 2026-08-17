package com.shirin.orderservice.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryEventLogger {

    private static final String RETRY_NAME = "inventory-service";
    private final RetryRegistry retryRegistry;

    @PostConstruct
    public void registerListeners() {

        Retry retry = retryRegistry.retry(RETRY_NAME);

        retry.getEventPublisher()
                .onRetry(event ->
                        log.warn(
                                "Retry event: name={}, attempt={}, wait={}ms, cause={}",
                                event.getName(),
                                event.getNumberOfRetryAttempts(),
                                event.getWaitInterval().toMillis(),
                                event.getLastThrowable() == null
                                        ? "none"
                                        : event.getLastThrowable().getClass().getSimpleName()

                        )
                )
                .onSuccess(event ->
                        log.info(
                                "Retry success: name={}, retryAttempts={}",
                                event.getName(),
                                event.getNumberOfRetryAttempts()
                        )
                )
                .onError(event ->
                        log.error(
                                "Retry exhausted: name={}, retryAttempts={}, cause={}",
                                event.getName(),
                                event.getNumberOfRetryAttempts(),
                                event.getLastThrowable() == null
                                        ? "none"
                                        : event.getLastThrowable().getClass().getSimpleName()
                        )
                );
    }
}
