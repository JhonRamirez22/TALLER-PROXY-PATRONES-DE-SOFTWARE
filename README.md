# Monitor de Microservicios con Proxy de Logging

- `backend/` — Spring Boot 3 (Java 21). `MicroserviceProxy<T>`, `LoggingProxy`, servicios Inventory/Orders/Payments, métricas y logs.
- `frontend/` — React + Vite + Chart.js. Dashboard con polling cada 3 s.

## Local
```bash
cd backend && mvn spring-boot:run          # http://localhost:8080
cd frontend && npm install && npm run dev  # http://localhost:5173
```

## API
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/services/inventory/{op}` | `listProducts`, `getStock`, `checkAvailability`, `addStock`, `removeStock` |
| POST | `/api/services/orders/{op}` | `createOrder`, `getOrder`, `listOrders`, `cancelOrder` |
| POST | `/api/services/payments/{op}` | `charge`, `getPayment`, `listPayments`, `refund` (10 % de fallos simulados) |
| GET | `/api/metrics/summary` | totales, tasa de error y tiempo promedio por servicio |
| GET | `/api/metrics/logs?service=&status=&from=&to=&page=&size=` | logs filtrados y paginados |
| POST | `/api/metrics/simulate-load` | 50 llamadas aleatorias |

Body de las operaciones: JSON, p. ej. `{"sku":"SKU-001","quantity":2}`.

## Despliegue (PROD)
**Backend → Railway** (root directory `backend`, usa el `Dockerfile`). Variable: `CORS_ALLOWED_ORIGINS=https://<tu-app>.vercel.app`.
**Frontend → Vercel** (root directory `frontend`). Variable: `VITE_API_URL=https://<tu-backend>.up.railway.app` (sin `/` final).
Despliega primero el backend, luego el frontend con su URL.
