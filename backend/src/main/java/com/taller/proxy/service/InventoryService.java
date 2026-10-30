package com.taller.proxy.service;

import com.taller.proxy.proxy.ServiceException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InventoryService extends AbstractMicroservice {

    public static final class Product {
        public final String sku;
        public final String name;
        public final double price;
        public int stock;

        Product(String sku, String name, double price, int stock) {
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }
    }

    private final Map<String, Product> products = new LinkedHashMap<>();

    public InventoryService() {
        super("inventory");
        add(new Product("SKU-001", "Laptop", 1200.0, 50));
        add(new Product("SKU-002", "Mouse", 25.0, 200));
        add(new Product("SKU-003", "Teclado", 70.0, 120));
        add(new Product("SKU-004", "Monitor", 300.0, 40));
        add(new Product("SKU-005", "Audifonos", 90.0, 80));

        register("listProducts", a -> List.copyOf(products.values()));
        register("getStock", a -> find(Args.str(a, "sku")));
        register("checkAvailability", a -> {
            Product p = find(Args.str(a, "sku"));
            int qty = Args.integer(a, "quantity");
            return Map.of("sku", p.sku, "requested", qty, "available", p.stock >= qty, "stock", p.stock);
        });
        register("addStock", a -> {
            Product p = find(Args.str(a, "sku"));
            int qty = Args.integer(a, "quantity");
            if (qty <= 0) throw new ServiceException(400, "quantity debe ser > 0");
            p.stock += qty;
            return p;
        });
        register("removeStock", a -> removeStock(Args.str(a, "sku"), Args.integer(a, "quantity")));
    }

    @Override
    protected void beforeExecute() {
        Args.simulateLatency(5, 60);
    }

    private void add(Product p) {
        products.put(p.sku, p);
    }

    public synchronized List<String> skus() {
        return List.copyOf(products.keySet());
    }

    public synchronized Product find(String sku) {
        Product p = products.get(sku);
        if (p == null) throw new ServiceException(404, "Producto no encontrado: " + sku);
        return p;
    }

    /** Usado tambien por OrderService al reservar stock. */
    public synchronized Product removeStock(String sku, int qty) {
        Product p = find(sku);
        if (qty <= 0) throw new ServiceException(400, "quantity debe ser > 0");
        if (p.stock < qty) {
            throw new ServiceException(409, "Stock insuficiente para " + sku + ": disponible " + p.stock + ", solicitado " + qty);
        }
        p.stock -= qty;
        return p;
    }

    public synchronized void restoreStock(String sku, int qty) {
        find(sku).stock += qty;
    }
}
