package com.taller.proxy.web;

import com.taller.proxy.metrics.LogEntry;
import com.taller.proxy.metrics.LogStore;
import com.taller.proxy.metrics.MetricsAggregator;
import com.taller.proxy.metrics.ServiceSummary;
import com.taller.proxy.proxy.ServiceException;
import com.taller.proxy.simulation.LoadSimulator;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final int SIMULATED_CALLS = 50;

    private final LogStore store;
    private final MetricsAggregator metrics;
    private final ProxyRegistry registry;
    private final LoadSimulator simulator;

    public MetricsController(LogStore store, MetricsAggregator metrics, ProxyRegistry registry, LoadSimulator simulator) {
        this.store = store;
        this.metrics = metrics;
        this.registry = registry;
        this.simulator = simulator;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<ServiceSummary> services = metrics.summary(registry.serviceIds());
        long total = services.stream().mapToLong(ServiceSummary::totalCalls).sum();
        long errors = services.stream().mapToLong(ServiceSummary::errorCalls).sum();
        double weightedAvg = total == 0 ? 0
                : services.stream().mapToDouble(s -> s.avgResponseTimeMs() * s.totalCalls()).sum() / total;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalCalls", total);
        out.put("totalErrors", errors);
        out.put("errorRate", total == 0 ? 0 : round(errors * 100.0 / total));
        out.put("avgResponseTimeMs", round(weightedAvg));
        out.put("services", services);
        return out;
    }

    @GetMapping("/logs")
    public LogStore.Page<LogEntry> logs(@RequestParam(required = false) String service,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String from,
                                        @RequestParam(required = false) String to,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return store.query(service, status, parseInstant(from, false), parseInstant(to, true), page, size);
    }

    @PostMapping("/simulate-load")
    public LoadSimulator.Result simulateLoad() {
        return simulator.run(SIMULATED_CALLS);
    }

    private static Instant parseInstant(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate d = LocalDate.parse(value);
                return (endOfDay ? d.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1) : d.atStartOfDay(ZoneOffset.UTC)).toInstant();
            } catch (DateTimeParseException ex) {
                throw new ServiceException(400, "Fecha invalida (usa ISO-8601): " + value);
            }
        }
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
