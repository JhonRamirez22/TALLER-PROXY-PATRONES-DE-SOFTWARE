package com.taller.proxy.service;

import com.taller.proxy.proxy.MicroserviceProxy;
import com.taller.proxy.proxy.ServiceException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InventoryService implements MicroserviceProxy<Object> {

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
        add(new Product("SKU-001", "Laptop", 1200.0, 50));
        add(new Product("SKU-002", "Mouse", 25.0, 200));
        add(new Product("SKU-003", "Teclado", 70.0, 120));
        add(new Product("SKU-004", "Monitor", 300.0, 40));
        add(new Product("SKU-005", "Audifonos", 90.0, 80));
    }

    private void add(Product p) {
        products.put(p.sku, p);
    }

    public List<String> skus() {
        return List.copyOf(products.keySet());
    }

    public synchronized Product find(String sku) {
        Product p = products.get(sku);
        if (p == null) throw new ServiceException(404, "Producto no encontrado: " + sku);
        return p;
    }

    @Override
    public synchronized Object execute(String operation, Object... params) {
        Args.simulateLatency(5, 60);
        Map<String, Object> args = Args.map(params);
        return switch (operation) {
            case "listProducts" -> List.copyOf(products.values());
            case "getStock" -> find(Args.str(args, "sku"));
            case "checkAvailability" -> {
                Product p = find(Args.str(args, "sku"));
                int qty = Args.integer(args, "quantity");
                yield Map.of("sku", p.sku, "requested", qty, "available", p.stock >= qty, "stock", p.stock);
            }
            case "addStock" -> {
                Product p = find(Args.str(args, "sku"));
                int qty = Args.integer(args, "quantity");
                if (qty <= 0) throw new ServiceException(400, "quantity debe ser > 0");
                p.stock += qty;
                yield p;
            }
            case "removeStock" -> removeStock(Args.str(args, "sku"), Args.integer(args, "quantity"));
            default -> throw Args.unknownOperation("inventory", operation);
        };
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
