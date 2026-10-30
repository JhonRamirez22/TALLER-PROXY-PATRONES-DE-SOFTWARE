package com.taller.proxy;

import com.taller.proxy.metrics.LogEntry;
import com.taller.proxy.metrics.LogStore;
import com.taller.proxy.proxy.LoggingProxy;
import com.taller.proxy.proxy.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggingProxyTest {

    @Test
    void logsSuccessAndError() {
        LogStore store = new LogStore(100);
        LoggingProxy<Object> proxy = new LoggingProxy<>("svc", (op, params) -> {
            if (op.equals("boom")) throw new ServiceException(500, "fallo");
            return "ok";
        }, store);

        assertEquals("ok", proxy.execute("fine"));
        assertThrows(ServiceException.class, () -> proxy.execute("boom"));

        var page = store.query("svc", null, null, null, 0, 10);
        assertEquals(2, page.totalElements());
        assertEquals(LogEntry.ERROR, page.content().get(0).status());
        assertTrue(page.content().get(0).error().contains("fallo"));
        assertEquals(LogEntry.SUCCESS, page.content().get(1).status());
        assertNotNull(page.content().get(1).requestId());
    }
}
