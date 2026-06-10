package com.shirin.outboxdemo.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    // status = 'FAILED' AND next_retry_at IS NULL : defensive query
    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE (
            status = 'PENDING'
            OR
            (
                status = 'FAILED'
                AND (next_retry_at IS NULL OR next_retry_at <= NOW())
            )
        )
        ORDER BY created_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true)
    List<OutboxEvent> findCandidatesForPublish(@Param("limit") int limit);

    @Modifying
    @Query(value = """
        UPDATE outbox_events
        SET status = 'FAILED',
            last_error = :error,
            next_retry_at = NOW(),
            processing_started_at = NULL,
            processing_by = NULL
        WHERE status = 'PROCESSING'
          AND processing_started_at < :threshold
        """, nativeQuery = true)
    int resetStuckProcessingEvents(
            @Param("threshold") Instant threshold,
            @Param("error") String error
    );

    @Modifying
    @Query(value = """
    UPDATE outbox_events
    SET status = 'PUBLISHED',
        published_at = NOW(),
        published_by = :owner,
        last_error = NULL,
        next_retry_at = NULL,
        processing_started_at = NULL,
        processing_by = NULL
    WHERE id = :eventId
      AND status = 'PROCESSING'
      AND processing_by = :owner
    """, nativeQuery = true)
    int markPublishedByOwner(
            @Param("eventId") UUID eventId,
            @Param("owner") String owner
    );

    @Modifying
    @Query(value = """
    UPDATE outbox_events
    SET status = 
        CASE 
             WHEN retry_count + 1 >= :maxRetries THEN 'DEAD' 
             ELSE 'FAILED' 
        END ,
        retry_count = retry_count + 1,
        last_error = :error,
        next_retry_at = 
        CASE 
            WHEN retry_count + 1 >= :maxRetries THEN NULL 
            ELSE NOW() + ((retry_count + 1) * INTERVAL '10 seconds')
        END ,
        processing_started_at = NULL,
        processing_by = NULL
    WHERE id = :eventId
      AND status = 'PROCESSING'
      AND processing_by = :owner
    """, nativeQuery = true)
    int markFailedOrDeadByOwner(
            @Param("eventId") UUID eventId,
            @Param("owner") String owner,
            @Param("error") String error,
            @Param("maxRetries") int maxRetries
    );

    long countByStatus(OutboxStatus status);


}

