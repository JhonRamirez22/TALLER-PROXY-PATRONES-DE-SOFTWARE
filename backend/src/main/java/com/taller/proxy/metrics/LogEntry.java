package com.taller.proxy.metrics;

import java.time.Instant;

public record LogEntry(
        String requestId,
        String serviceId,
        String operation,
        long durationMs,
        String status,
        Instant timestamp,
        Object params,
        Object response,
        String error) {

    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
}
