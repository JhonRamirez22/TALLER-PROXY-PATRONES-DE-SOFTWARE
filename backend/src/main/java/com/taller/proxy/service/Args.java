package com.taller.proxy.service;

import com.taller.proxy.proxy.ServiceException;

import java.util.Map;

/** Utilidades para leer los parametros (un Map JSON) que llegan a cada operacion. */
final class Args {
    private Args() {}

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object[] params) {
        if (params != null && params.length > 0 && params[0] instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Map.of();
    }

    static String str(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null || v.toString().isBlank()) {
            throw new ServiceException(400, "Parametro requerido: " + key);
        }
        return v.toString();
    }

    static String str(Map<String, Object> args, String key, String def) {
        Object v = args.get(key);
        return v == null || v.toString().isBlank() ? def : v.toString();
    }

    static int integer(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) throw new ServiceException(400, "Parametro requerido: " + key);
        try {
            return v instanceof Number n ? n.intValue() : Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            throw new ServiceException(400, "Parametro invalido (entero): " + key);
        }
    }

    static double decimal(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) throw new ServiceException(400, "Parametro requerido: " + key);
        try {
            return v instanceof Number n ? n.doubleValue() : Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            throw new ServiceException(400, "Parametro invalido (numero): " + key);
        }
    }

    static ServiceException unknownOperation(String service, String operation) {
        return new ServiceException(404, "Operacion desconocida en " + service + ": " + operation);
    }

    /** Simula latencia de red/BD para que las metricas tengan valores significativos. */
    static void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextInt(minMs, maxMs + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
