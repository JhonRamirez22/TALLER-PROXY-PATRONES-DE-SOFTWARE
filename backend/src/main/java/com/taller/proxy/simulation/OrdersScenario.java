package com.taller.proxy.simulation;

import com.taller.proxy.service.InventoryService;
import com.taller.proxy.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrdersScenario implements LoadScenario {

    private static final String[] CUSTOMERS = {"Ana", "Luis", "Marta", "Pedro", "Sofia"};
    private final OrderService orders;
    private final InventoryService inventory;

    public OrdersScenario(OrderService orders, InventoryService inventory) {
        this.orders = orders;
        this.inventory = inventory;
    }

    @Override public String serviceId() { return "orders"; }

    @Override
    public PlannedCall next() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Optional<String> id = orders.anyOrderId();
        int pick = r.nextInt(3);
        if (pick == 0 || id.isEmpty()) {
            var skus = inventory.skus();
            return new PlannedCall("createOrder", Map.of(
                    "sku", skus.get(r.nextInt(skus.size())),
                    "quantity", r.nextInt(1, 4),
                    "customer", CUSTOMERS[r.nextInt(CUSTOMERS.length)]));
        }
        return pick == 1 ? new PlannedCall("getOrder", Map.of("orderId", id.get()))
                         : new PlannedCall("listOrders", Map.of());
    }
}
