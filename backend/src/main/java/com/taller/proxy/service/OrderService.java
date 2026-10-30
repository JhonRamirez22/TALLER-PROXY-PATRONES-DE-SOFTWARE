package com.taller.proxy.service;

import com.taller.proxy.proxy.ServiceException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class OrderService extends AbstractMicroservice {

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
        super("orders");
        this.inventory = inventory;

        register("createOrder", a -> {
            String sku = Args.str(a, "sku");
            int qty = Args.integer(a, "quantity");
            String customer = Args.str(a, "customer", "anonimo");
            InventoryService.Product p = inventory.removeStock(sku, qty);
            Order o = new Order(customer, sku, qty, p.price * qty);
            orders.put(o.orderId, o);
            return o;
        });
        register("getOrder", a -> find(Args.str(a, "orderId")));
        register("listOrders", a -> List.copyOf(orders.values()));
        register("cancelOrder", a -> {
            Order o = find(Args.str(a, "orderId"));
            if ("CANCELLED".equals(o.status)) throw new ServiceException(409, "La orden ya esta cancelada");
            o.status = "CANCELLED";
            inventory.restoreStock(o.sku, o.quantity);
            return o;
        });
    }

    @Override
    protected void beforeExecute() {
        Args.simulateLatency(10, 90);
    }

    public synchronized Optional<String> anyOrderId() {
        return orders.keySet().stream().skip(orders.isEmpty() ? 0 : new Random().nextInt(orders.size())).findFirst();
    }

    private Order find(String id) {
        Order o = orders.get(id);
        if (o == null) throw new ServiceException(404, "Orden no encontrada: " + id);
        return o;
    }
}
