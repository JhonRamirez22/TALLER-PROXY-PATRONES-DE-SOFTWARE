package com.taller.proxy.metrics;

import org.springframework.stereotype.Component;

import java.util.*;

/** Observer que mantiene los contadores acumulados por servicio. */
@Component
public class MetricsAggregator implements LogObserver {

    private static final class Stats {
        long total, errors, sumDurationMs;
    }

    private final Map<String, Stats> stats = new LinkedHashMap<>();

    @Override
    public synchronized void onLog(LogEntry entry) {
        Stats s = stats.computeIfAbsent(entry.serviceId(), k -> new Stats());
        s.total++;
        s.sumDurationMs += entry.durationMs();
        if (LogEntry.ERROR.equals(entry.status())) s.errors++;
    }

    public synchronized List<ServiceSummary> summary(Collection<String> serviceIds) {
        List<ServiceSummary> result = new ArrayList<>();
        for (String id : serviceIds) {
            Stats s = stats.getOrDefault(id, new Stats());
            result.add(ServiceSummary.of(id, s.total, s.errors, s.sumDurationMs));
        }
        return result;
    }
}
