package com.taller.proxy.service;

import com.taller.proxy.proxy.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService extends AbstractMicroservice {

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
        super("payments");
        this.failureRate = failureRate;

        register("charge", a -> {
            double amount = Args.decimal(a, "amount");
            if (amount <= 0) throw new ServiceException(400, "amount debe ser > 0");
            Payment p = new Payment(Args.str(a, "orderId"), amount, Args.str(a, "method", "CARD"));
            payments.put(p.paymentId, p);
            return p;
        });
        register("getPayment", a -> find(Args.str(a, "paymentId")));
        register("listPayments", a -> List.copyOf(payments.values()));
        register("refund", a -> {
            Payment p = find(Args.str(a, "paymentId"));
            if ("REFUNDED".equals(p.status)) throw new ServiceException(409, "El pago ya fue reembolsado");
            p.status = "REFUNDED";
            return p;
        });
    }

    /** Hook del Template Method: latencia y falla intencional (~10 %) en cualquier operacion de pagos. */
    @Override
    protected void beforeExecute() {
        Args.simulateLatency(20, 150);
        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new ServiceException(502, "Pasarela de pagos no disponible (fallo simulado)");
        }
    }

    public synchronized Optional<String> anyPaymentId() {
        return payments.keySet().stream().skip(payments.isEmpty() ? 0 : new Random().nextInt(payments.size())).findFirst();
    }

    private Payment find(String id) {
        Payment p = payments.get(id);
        if (p == null) throw new ServiceException(404, "Pago no encontrado: " + id);
        return p;
    }
}
