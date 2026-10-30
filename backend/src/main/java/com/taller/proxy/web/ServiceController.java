package com.taller.proxy.web;

import com.taller.proxy.proxy.RequestContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ProxyRegistry registry;

    public ServiceController(ProxyRegistry registry) {
        this.registry = registry;
    }

    @PostMapping({"/inventory/{operation}", "/orders/{operation}", "/payments/{operation}"})
    public ResponseEntity<Map<String, Object>> call(@PathVariable String operation,
                                                    @RequestBody(required = false) Map<String, Object> body,
                                                    jakarta.servlet.http.HttpServletRequest request) {
        // /api/services/{service}/{operation}
        String[] parts = request.getRequestURI().split("/");
        String service = parts[parts.length - 2];
        RequestContext.clear(); // el handler de errores lee el requestId asignado por el proxy
        Object result = registry.get(service).execute(operation, body == null ? Map.of() : body);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("requestId", RequestContext.get());
        out.put("service", service);
        out.put("operation", operation);
        out.put("status", "SUCCESS");
        out.put("result", result);
        RequestContext.clear();
        return ResponseEntity.ok(out);
    }
}
