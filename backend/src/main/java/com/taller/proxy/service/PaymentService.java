package com.taller.proxy.service;

import com.taller.proxy.proxy.MicroserviceProxy;
import com.taller.proxy.proxy.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService implements MicroserviceProxy<Object> {

    public static final class Payment {
        public final String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        public final String orderId;
        public final double amount;
        public final String method;
        public final Instant createdAt = Instant.now();
        public String status = "APPROVED";

        Payment(String orderId, double amount, String method) {
            this.orderId = orderId;
            this.amount = amount;
            this.method = method;
        }
    }

    private final double failureRate;
    private final Map<String, Payment> payments = new LinkedHashMap<>();

    public PaymentService(@Value("${app.payment.failure-rate:0.10}") double failureRate) {
        this.failureRate = failureRate;
    }

    public synchronized Optional<String> anyPaymentId() {
        return payments.keySet().stream().skip(payments.isEmpty() ? 0 : new Random().nextInt(payments.size())).findFirst();
    }

    @Override
    public synchronized Object execute(String operation, Object... params) {
        Args.simulateLatency(20, 150);
        // Falla intencional: ~10% de las llamadas al servicio de pagos
        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new ServiceException(502, "Pasarela de pagos no disponible (fallo simulado)");
        }
        Map<String, Object> args = Args.map(params);
        return switch (operation) {
            case "charge" -> {
                double amount = Args.decimal(args, "amount");
                if (amount <= 0) throw new ServiceException(400, "amount debe ser > 0");
                Payment p = new Payment(Args.str(args, "orderId"), amount, Args.str(args, "method", "CARD"));
                payments.put(p.paymentId, p);
                yield p;
            }
            case "getPayment" -> find(Args.str(args, "paymentId"));
            case "listPayments" -> List.copyOf(payments.values());
            case "refund" -> {
                Payment p = find(Args.str(args, "paymentId"));
                if ("REFUNDED".equals(p.status)) throw new ServiceException(409, "El pago ya fue reembolsado");
                p.status = "REFUNDED";
                yield p;
            }
            default -> throw Args.unknownOperation("payments", operation);
        };
    }

    private Payment find(String id) {
        Payment p = payments.get(id);
        if (p == null) throw new ServiceException(404, "Pago no encontrado: " + id);
        return p;
    }
}
