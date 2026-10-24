package com.electromart.dto;

import java.time.Instant;

/** Uniform error body. Never contains stack traces or internal exception messages. */
public record ApiError(
        int status,
        String code,
        String message,
        Instant timestamp
) {
}
