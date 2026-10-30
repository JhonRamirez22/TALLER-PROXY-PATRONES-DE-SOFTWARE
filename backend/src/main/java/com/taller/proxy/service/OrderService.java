package com.taller.proxy.service;

import com.taller.proxy.proxy.MicroserviceProxy;
import com.taller.proxy.proxy.ServiceException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class OrderService implements MicroserviceProxy<Object> {

    public static final class Order {
        public final String orderId = UUID.randomUUID().toString().substring(0, 8);
        public final String customer;
        public final String sku;
        public final int quantity;
        public final double total;
        public final Instant createdAt = Instant.now();
        public String status = "CREATED";

        Order(String customer, String sku, int quantity, double total) {
            this.customer = customer;
            this.sku = sku;
            this.quantity = quantity;
            this.total = total;
        }
    }

    private final InventoryService inventory;
    private final Map<String, Order> orders = new LinkedHashMap<>();

    public OrderService(InventoryService inventory) {
        this.inventory = inventory;
    }

    public synchronized Optional<String> anyOrderId() {
        return orders.keySet().stream().skip(orders.isEmpty() ? 0 : new Random().nextInt(orders.size())).findFirst();
    }

    @Override
    public synchronized Object execute(String operation, Object... params) {
        Args.simulateLatency(10, 90);
        Map<String, Object> args = Args.map(params);
        return switch (operation) {
            case "createOrder" -> {
                String sku = Args.str(args, "sku");
                int qty = Args.integer(args, "quantity");
                String customer = Args.str(args, "customer", "anonimo");
                InventoryService.Product p = inventory.removeStock(sku, qty);
                Order o = new Order(customer, sku, qty, p.price * qty);
                orders.put(o.orderId, o);
                yield o;
            }
            case "getOrder" -> find(Args.str(args, "orderId"));
            case "listOrders" -> List.copyOf(orders.values());
            case "cancelOrder" -> {
                Order o = find(Args.str(args, "orderId"));
                if ("CANCELLED".equals(o.status)) throw new ServiceException(409, "La orden ya esta cancelada");
                o.status = "CANCELLED";
                inventory.restoreStock(o.sku, o.quantity);
                yield o;
            }
            default -> throw Args.unknownOperation("orders", operation);
        };
    }

    private Order find(String id) {
        Order o = orders.get(id);
        if (o == null) throw new ServiceException(404, "Orden no encontrada: " + id);
        return o;
    }
}
