package com.taller.proxy.service;

import com.taller.proxy.proxy.MicroserviceProxy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Patron Template Method: execute() fija el algoritmo (hook previo -> resolver operacion -> ejecutar)
 * y las subclases solo registran sus operaciones (Command) y personalizan el hook.
 * Sustituye al switch por servicio: agregar una operacion no modifica execute() (Open/Closed).
 */
public abstract class AbstractMicroservice implements MicroserviceProxy<Object> {

    private final String name;
    private final Map<String, Operation> operations = new LinkedHashMap<>();

    protected AbstractMicroservice(String name) {
        this.name = name;
    }

    protected final void register(String operation, Operation command) {
        operations.put(operation, command);
    }

    /** Paso variable del algoritmo: latencia simulada, fallos intencionales, etc. */
    protected void beforeExecute() {}

    @Override
    public final Object execute(String operation, Object... params) {
        beforeExecute();
        Operation command = operations.get(operation);
        if (command == null) {
            throw Args.unknownOperation(name, operation);
        }
        synchronized (this) {
            return command.run(Args.map(params));
        }
    }
}
