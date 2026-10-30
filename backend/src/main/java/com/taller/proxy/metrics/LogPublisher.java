package com.taller.proxy.metrics;

import org.springframework.stereotype.Component;

import java.util.List;

/** Sujeto del patron Observer: el proxy publica y los observadores (almacen, metricas) reaccionan. */
@Component
public class LogPublisher {

    private final List<LogObserver> observers;

    /** Spring inyecta todos los beans LogObserver: agregar uno nuevo no toca al proxy. */
    public LogPublisher(List<LogObserver> observers) {
        this.observers = List.copyOf(observers);
    }

    public void publish(LogEntry entry) {
        observers.forEach(o -> o.onLog(entry));
    }
}
