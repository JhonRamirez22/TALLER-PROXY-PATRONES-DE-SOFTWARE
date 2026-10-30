package com.taller.proxy.simulation;

import com.taller.proxy.service.OrderService;
import com.taller.proxy.service.PaymentService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentsScenario implements LoadScenario {

    private final PaymentService payments;
    private final OrderService orders;

    public PaymentsScenario(PaymentService payments, OrderService orders) {
        this.payments = payments;
        this.orders = orders;
    }

    @Override public String serviceId() { return "payments"; }

    @Override
    public PlannedCall next() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Optional<String> id = payments.anyPaymentId();
        int pick = r.nextInt(3);
        if (pick == 0 || id.isEmpty()) {
            return new PlannedCall("charge", Map.of(
                    "orderId", orders.anyOrderId().orElse("N/A"),
                    "amount", Math.round(r.nextDouble(10, 500) * 100) / 100.0,
                    "method", r.nextBoolean() ? "CARD" : "PSE"));
        }
        return pick == 1 ? new PlannedCall("getPayment", Map.of("paymentId", id.get()))
                         : new PlannedCall("listPayments", Map.of());
    }
}
