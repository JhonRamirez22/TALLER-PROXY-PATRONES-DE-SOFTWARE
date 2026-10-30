package com.taller.proxy.simulation;

import com.taller.proxy.service.InventoryService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class InventoryScenario implements LoadScenario {

    private static final String[] OPS = {"listProducts", "getStock", "checkAvailability", "addStock"};
    private final InventoryService inventory;

    public InventoryScenario(InventoryService inventory) {
        this.inventory = inventory;
    }

    @Override public String serviceId() { return "inventory"; }

    @Override
    public PlannedCall next() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        var skus = inventory.skus();
        return new PlannedCall(OPS[r.nextInt(OPS.length)],
                Map.of("sku", skus.get(r.nextInt(skus.size())), "quantity", r.nextInt(1, 10)));
    }
}
