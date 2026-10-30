package com.taller.proxy.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/** Observer que guarda los logs recientes en memoria (acotado) y los consulta con filtros y paginacion. */
@Component
public class LogStore implements LogObserver {

    private final int maxEntries;
    private final Deque<LogEntry> entries = new ArrayDeque<>(); // mas reciente primero

    public LogStore(@Value("${app.logs.max-entries:5000}") int maxEntries) {
        this.maxEntries = maxEntries;
    }

    @Override
    public synchronized void onLog(LogEntry entry) {
        entries.addFirst(entry);
        while (entries.size() > maxEntries) {
            entries.removeLast();
        }
    }

    public synchronized Page<LogEntry> query(String service, String status, Instant from, Instant to, int page, int size) {
        List<LogEntry> filtered = entries.stream()
                .filter(e -> isBlank(service) || e.serviceId().equalsIgnoreCase(service))
                .filter(e -> isBlank(status) || e.status().equalsIgnoreCase(status))
                .filter(e -> from == null || !e.timestamp().isBefore(from))
                .filter(e -> to == null || !e.timestamp().isAfter(to))
                .toList();

        int safeSize = Math.max(1, Math.min(size, 200));
        int safePage = Math.max(0, page);
        int fromIdx = Math.min(safePage * safeSize, filtered.size());
        int toIdx = Math.min(fromIdx + safeSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) safeSize);
        return new Page<>(filtered.subList(fromIdx, toIdx), safePage, safeSize, filtered.size(), totalPages);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
