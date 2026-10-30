package com.taller.proxy.simulation;

/** Patron Strategy: cada servicio decide como generar su siguiente llamada de prueba. */
public interface LoadScenario {
    String serviceId();

    PlannedCall next();
}
