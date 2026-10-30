package com.taller.proxy.proxy;

/** Expone el requestId de la ultima llamada del hilo actual para poder devolverlo en la respuesta HTTP. */
public final class RequestContext {
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(String requestId) { REQUEST_ID.set(requestId); }
    public static String get() { return REQUEST_ID.get(); }
    public static void clear() { REQUEST_ID.remove(); }
}
