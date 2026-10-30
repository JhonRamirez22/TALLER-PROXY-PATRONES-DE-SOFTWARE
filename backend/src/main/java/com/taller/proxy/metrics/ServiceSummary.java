package com.taller.proxy.metrics;

public record ServiceSummary(
        String serviceId,
        long totalCalls,
        long successCalls,
        long errorCalls,
        double successRate,
        double errorRate,
        double avgResponseTimeMs) {

    static ServiceSummary of(String id, long total, long errors, long sumMs) {
        double errorRate = total == 0 ? 0 : round(errors * 100.0 / total);
        double successRate = total == 0 ? 0 : round(100.0 - errorRate);
        double avg = total == 0 ? 0 : round(sumMs / (double) total);
        return new ServiceSummary(id, total, total - errors, errors, successRate, errorRate, avg);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
