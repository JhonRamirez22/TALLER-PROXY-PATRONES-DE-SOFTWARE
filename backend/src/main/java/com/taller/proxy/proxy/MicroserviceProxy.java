package com.taller.proxy.proxy;

/** Contrato comun para servicios reales y para el proxy que los envuelve. */
public interface MicroserviceProxy<T> {
    T execute(String operation, Object... params);
}
