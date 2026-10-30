package com.taller.proxy.web;

import com.taller.proxy.metrics.LogStore;
import com.taller.proxy.proxy.LoggingProxy;
import com.taller.proxy.proxy.MicroserviceProxy;
import com.taller.proxy.proxy.ServiceException;
import com.taller.proxy.service.InventoryService;
import com.taller.proxy.service.OrderService;
import com.taller.proxy.service.PaymentService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Cada servicio real queda detras de su propio LoggingProxy. */
@Component
public class ProxyRegistry {

    private final Map<String, MicroserviceProxy<Object>> proxies = new LinkedHashMap<>();

    public ProxyRegistry(InventoryService inventory, OrderService orders, PaymentService payments, LogStore store) {
        proxies.put("inventory", new LoggingProxy<>("inventory", inventory, store));
        proxies.put("orders", new LoggingProxy<>("orders", orders, store));
        proxies.put("payments", new LoggingProxy<>("payments", payments, store));
    }

    public MicroserviceProxy<Object> get(String serviceId) {
        MicroserviceProxy<Object> p = proxies.get(serviceId);
        if (p == null) throw new ServiceException(404, "Servicio desconocido: " + serviceId);
        return p;
    }

    public Set<String> serviceIds() {
        return proxies.keySet();
    }
}
