package com.taller.proxy.web;

import com.taller.proxy.metrics.LogEntry;
import com.taller.proxy.metrics.LogStore;
import com.taller.proxy.metrics.ServiceSummary;
import com.taller.proxy.proxy.ServiceException;
import com.taller.proxy.service.InventoryService;
import com.taller.proxy.service.OrderService;
import com.taller.proxy.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final LogStore store;
    private final ProxyRegistry registry;
    private final InventoryService inventory;
    private final OrderService orders;
    private final PaymentService payments;

    public MetricsController(LogStore store, ProxyRegistry registry, InventoryService inventory,
                             OrderService orders, PaymentService payments) {
        this.store = store;
        this.registry = registry;
        this.inventory = inventory;
        this.orders = orders;
        this.payments = payments;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<ServiceSummary> services = store.summary(registry.serviceIds());
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
    public Map<String, Object> simulateLoad() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String[] names = {"Ana", "Luis", "Marta", "Pedro", "Sofia"};
        int ok = 0, failed = 0;
        for (int i = 0; i < 50; i++) {
            String service = registry.serviceIds().stream().skip(r.nextInt(3)).findFirst().orElseThrow();
            String sku = inventory.skus().get(r.nextInt(inventory.skus().size()));
            String op;
            Map<String, Object> p = new HashMap<>();
            switch (service) {
                case "inventory" -> {
                    op = new String[]{"listProducts", "getStock", "checkAvailability", "addStock"}[r.nextInt(4)];
                    p.put("sku", sku);
                    p.put("quantity", r.nextInt(1, 10));
                }
                case "orders" -> {
                    Optional<String> id = orders.anyOrderId();
                    int pick = r.nextInt(3);
                    if (pick == 0 || id.isEmpty()) {
                        op = "createOrder";
                        p.put("sku", sku);
                        p.put("quantity", r.nextInt(1, 4));
                        p.put("customer", names[r.nextInt(names.length)]);
                    } else if (pick == 1) {
                        op = "getOrder";
                        p.put("orderId", id.get());
                    } else {
                        op = "listOrders";
                    }
                }
                default -> {
                    Optional<String> id = payments.anyPaymentId();
                    int pick = r.nextInt(3);
                    if (pick == 0 || id.isEmpty()) {
                        op = "charge";
                        p.put("orderId", orders.anyOrderId().orElse("N/A"));
                        p.put("amount", Math.round(r.nextDouble(10, 500) * 100) / 100.0);
                        p.put("method", r.nextBoolean() ? "CARD" : "PSE");
                    } else if (pick == 1) {
                        op = "getPayment";
                        p.put("paymentId", id.get());
                    } else {
                        op = "listPayments";
                    }
                }
            }
            try {
                registry.get(service).execute(op, p);
                ok++;
            } catch (RuntimeException e) {
                failed++;
            }
        }
        return Map.of("generated", 50, "success", ok, "errors", failed);
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
