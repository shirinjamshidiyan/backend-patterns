package com.shirin.outboxdemo.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        String path,
        String message,
        List<String> errors
) {
}
