package com.taller.proxy.metrics;

/** Patron Observer: cualquier componente interesado en los logs se suscribe implementando esta interfaz. */
public interface LogObserver {
    void onLog(LogEntry entry);
}
