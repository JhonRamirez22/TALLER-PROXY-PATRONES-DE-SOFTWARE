package com.taller.proxy.proxy;

import com.taller.proxy.metrics.LogPublisher;
import org.springframework.stereotype.Component;

/** Patron Factory: unico lugar que sabe como armar un LoggingProxy para cualquier servicio. */
@Component
public class ProxyFactory {

    private final LogPublisher publisher;

    public ProxyFactory(LogPublisher publisher) {
        this.publisher = publisher;
    }

    public <T> MicroserviceProxy<T> create(String serviceId, MicroserviceProxy<T> target) {
        return new LoggingProxy<>(serviceId, target, publisher);
    }
}
