package com.taller.proxy.proxy;

/** Error de negocio con el codigo HTTP que debe devolver la API. */
public class ServiceException extends RuntimeException {
    private final int httpStatus;

    public ServiceException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
