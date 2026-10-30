package com.taller.proxy;

import com.taller.proxy.metrics.LogEntry;
import com.taller.proxy.metrics.LogPublisher;
import com.taller.proxy.metrics.LogStore;
import com.taller.proxy.metrics.MetricsAggregator;
import com.taller.proxy.proxy.LoggingProxy;
import com.taller.proxy.proxy.ServiceException;
import com.taller.proxy.service.AbstractMicroservice;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoggingProxyTest {

    /** Servicio minimo: demuestra Template Method + Command sin depender de los servicios reales. */
    static class Dummy extends AbstractMicroservice {
        Dummy() {
            super("svc");
            register("fine", a -> "ok");
            register("boom", a -> { throw new ServiceException(500, "fallo"); });
        }
    }

    @Test
    void proxyPublishesToAllObservers() {
        LogStore store = new LogStore(100);
        MetricsAggregator metrics = new MetricsAggregator();
        LoggingProxy<Object> proxy = new LoggingProxy<>("svc", new Dummy(), new LogPublisher(List.of(store, metrics)));

        assertEquals("ok", proxy.execute("fine"));
        assertThrows(ServiceException.class, () -> proxy.execute("boom"));
        assertThrows(ServiceException.class, () -> proxy.execute("noExiste"));

        var page = store.query("svc", null, null, null, 0, 10);
        assertEquals(3, page.totalElements());
        assertEquals(LogEntry.ERROR, page.content().get(1).status());
        assertTrue(page.content().get(1).error().contains("fallo"));
        assertEquals(LogEntry.SUCCESS, page.content().get(2).status());
        assertNotNull(page.content().get(2).requestId());

        var summary = metrics.summary(List.of("svc")).get(0);
        assertEquals(3, summary.totalCalls());
        assertEquals(2, summary.errorCalls());
    }
}
