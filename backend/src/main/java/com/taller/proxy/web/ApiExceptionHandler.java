package com.taller.proxy.web;

import com.taller.proxy.proxy.RequestContext;
import com.taller.proxy.proxy.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleService(ServiceException e) {
        return build(e.getHttpStatus(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        return build(500, e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("requestId", RequestContext.get());
        body.put("status", "ERROR");
        body.put("message", message);
        RequestContext.clear();
        return ResponseEntity.status(status).body(body);
    }
}
