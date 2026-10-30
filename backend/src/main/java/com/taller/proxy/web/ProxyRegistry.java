package com.taller.proxy.web;

import com.taller.proxy.proxy.MicroserviceProxy;
import com.taller.proxy.proxy.ProxyFactory;
import com.taller.proxy.proxy.ServiceException;
import com.taller.proxy.service.InventoryService;
import com.taller.proxy.service.OrderService;
import com.taller.proxy.service.PaymentService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Registro de servicios: cada servicio real queda detras de su proxy, creado por la ProxyFactory. */
@Component
public class ProxyRegistry {

    private final Map<String, MicroserviceProxy<Object>> proxies = new LinkedHashMap<>();

    public ProxyRegistry(InventoryService inventory, OrderService orders, PaymentService payments, ProxyFactory factory) {
        proxies.put("inventory", factory.create("inventory", inventory));
        proxies.put("orders", factory.create("orders", orders));
        proxies.put("payments", factory.create("payments", payments));
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
