import { AlertIcon, CheckIcon } from './icons'

export const ERROR_THRESHOLD = 15
export const SERVICE_META = {
  inventory: { label: 'Inventario', color: 'var(--svc-inventory)' },
  orders: { label: 'Pedidos', color: 'var(--svc-orders)' },
  payments: { label: 'Pagos', color: 'var(--svc-payments)' },
}

export default function ServiceCard({ summary }) {
  const meta = SERVICE_META[summary.serviceId] || { label: summary.serviceId, color: 'var(--ink-3)' }
  const hasData = summary.totalCalls > 0
  const unhealthy = summary.errorRate > ERROR_THRESHOLD

  return (
    <article className={`svc ${unhealthy ? 'svc-bad' : ''}`} aria-label={`Servicio ${meta.label}`}>
      <header className="svc-head">
        <span className="swatch" style={{ background: meta.color }} aria-hidden="true" />
        <h3>{meta.label}</h3>
        <span className={`chip ${unhealthy ? 'chip-bad' : hasData ? 'chip-ok' : 'chip-idle'}`}>
          {unhealthy ? <AlertIcon width={14} height={14} /> : hasData ? <CheckIcon width={14} height={14} /> : null}
          {unhealthy ? 'Con problemas' : hasData ? 'Saludable' : 'Sin datos'}
        </span>
      </header>

      <dl className="svc-stats">
        <div>
          <dt>Llamadas</dt>
          <dd>{summary.totalCalls.toLocaleString('es')}</dd>
        </div>
        <div>
          <dt>Tasa de éxito</dt>
          <dd>{hasData ? `${summary.successRate.toFixed(1)}%` : '—'}</dd>
        </div>
        <div>
          <dt>Tiempo promedio</dt>
          <dd>{hasData ? <>{Math.round(summary.avgResponseTimeMs)}<small> ms</small></> : '—'}</dd>
        </div>
      </dl>

      <div className="errbar" role="img"
        aria-label={`Tasa de error ${summary.errorRate}%, umbral ${ERROR_THRESHOLD}%`}>
        <div className="errbar-track">
          <div className="errbar-fill" style={{ width: `${Math.min(summary.errorRate, 100)}%` }} />
          <div className="errbar-limit" style={{ left: `${ERROR_THRESHOLD}%` }} />
        </div>
        <p>
          <span className="err-value">{summary.errorRate.toFixed(1)}% de error</span>
          <span>umbral {ERROR_THRESHOLD}%</span>
        </p>
      </div>
    </article>
  )
}
