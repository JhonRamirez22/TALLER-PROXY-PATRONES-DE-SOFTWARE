package com.taller.proxy.simulation;

import com.taller.proxy.web.ProxyRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Fachada de la simulacion: elige una estrategia al azar y ejecuta la llamada a traves del proxy. */
@Service
public class LoadSimulator {

    public record Result(int generated, int success, int errors) {}

    private final List<LoadScenario> scenarios;
    private final ProxyRegistry registry;

    public LoadSimulator(List<LoadScenario> scenarios, ProxyRegistry registry) {
        this.scenarios = scenarios;
        this.registry = registry;
    }

    public Result run(int calls) {
        int ok = 0, failed = 0;
        for (int i = 0; i < calls; i++) {
            LoadScenario scenario = scenarios.get(ThreadLocalRandom.current().nextInt(scenarios.size()));
            PlannedCall call = scenario.next();
            try {
                registry.get(scenario.serviceId()).execute(call.operation(), call.params());
                ok++;
            } catch (RuntimeException e) {
                failed++;
            }
        }
        return new Result(calls, ok, failed);
    }
}
