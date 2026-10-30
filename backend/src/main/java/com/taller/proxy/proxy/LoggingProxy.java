package com.taller.proxy.proxy;

import com.taller.proxy.metrics.LogEntry;
import com.taller.proxy.metrics.LogStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Proxy que envuelve cualquier MicroserviceProxy: registra inicio, duracion y resultado,
 * y si falla guarda el error con un stack trace resumido.
 */
public class LoggingProxy<T> implements MicroserviceProxy<T> {

    private static final Logger log = LoggerFactory.getLogger(LoggingProxy.class);

    private final String serviceId;
    private final MicroserviceProxy<T> delegate;
    private final LogStore store;

    public LoggingProxy(String serviceId, MicroserviceProxy<T> delegate, LogStore store) {
        this.serviceId = serviceId;
        this.delegate = delegate;
        this.store = store;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        RequestContext.set(requestId);
        Object loggedParams = params == null || params.length == 0 ? null
                : params.length == 1 ? params[0] : Arrays.asList(params);
        Instant timestamp = Instant.now();
        long start = System.nanoTime();
        log.info("[{}] START {}.{} params={}", requestId, serviceId, operation, loggedParams);

        try {
            T result = delegate.execute(operation, params);
            long ms = elapsedMs(start);
            log.info("[{}] SUCCESS {}.{} in {}ms", requestId, serviceId, operation, ms);
            store.add(new LogEntry(requestId, serviceId, operation, ms, LogEntry.SUCCESS, timestamp,
                    loggedParams, result, null));
            return result;
        } catch (RuntimeException e) {
            long ms = elapsedMs(start);
            String summary = summarize(e);
            log.warn("[{}] ERROR {}.{} in {}ms -> {}", requestId, serviceId, operation, ms, summary);
            store.add(new LogEntry(requestId, serviceId, operation, ms, LogEntry.ERROR, timestamp,
                    loggedParams, null, summary));
            throw e;
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    /** Clase + mensaje + las 3 primeras llamadas del stack. */
    static String summarize(Throwable e) {
        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0; i < Math.min(3, trace.length); i++) {
            sb.append("\n  at ").append(trace[i]);
        }
        return sb.toString();
    }
}
