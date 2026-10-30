import { useCallback, useEffect, useRef, useState } from 'react'
import { getLogs, getSummary, simulateLoad } from './api'
import ServiceCard from './components/ServiceCard'
import LatencyChart from './components/LatencyChart'
import Filters from './components/Filters'
import LogsTable from './components/LogsTable'
import { BoltIcon, SpinnerIcon } from './components/icons'

const SERVICES = ['inventory', 'orders', 'payments']
const POLL_MS = 3000
const PAGE_SIZE = 10
const EMPTY_PAGE = { content: [], page: 0, totalPages: 0, totalElements: 0 }
const iso = (local) => (local ? new Date(local).toISOString() : '')

export default function App() {
  const [summary, setSummary] = useState(null)
  const [series, setSeries] = useState({ inventory: [], orders: [], payments: [] })
  const [page, setPage] = useState(EMPTY_PAGE)
  const [pageNum, setPageNum] = useState(0)
  const [filters, setFilters] = useState({ service: '', status: '', from: '', to: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)
  const [updatedAt, setUpdatedAt] = useState(null)
  const noticeTimer = useRef()

  const refresh = useCallback(async () => {
    try {
      const [s, logs, ...chart] = await Promise.all([
        getSummary(),
        getLogs({
          service: filters.service, status: filters.status,
          from: iso(filters.from), to: iso(filters.to), page: pageNum, size: PAGE_SIZE,
        }),
        ...SERVICES.map((id) => getLogs({ service: id, size: 20 })),
      ])
      setSummary(s)
      setPage(logs)
      setSeries(Object.fromEntries(SERVICES.map((id, i) => [id, chart[i].content])))
      setUpdatedAt(new Date())
      setError(null)
    } catch (e) {
      setError(e.message)
    }
  }, [filters, pageNum])

  useEffect(() => {
    refresh()
    const t = setInterval(refresh, POLL_MS)
    return () => clearInterval(t)
  }, [refresh])

  const onSimulate = async () => {
    setLoading(true)
    try {
      const r = await simulateLoad()
      await refresh()
      setNotice(`Se generaron ${r.generated} llamadas: ${r.success} exitosas y ${r.errors} con error.`)
    } catch (e) {
      setNotice(`No se pudo simular la carga (${e.message}). Revisa que el backend esté activo.`)
    } finally {
      setLoading(false)
      clearTimeout(noticeTimer.current)
      noticeTimer.current = setTimeout(() => setNotice(null), 6000)
    }
  }

  const changeFilters = (f) => { setFilters(f); setPageNum(0) }
  const filtered = Object.values(filters).some(Boolean)

  return (
    <div className="shell">
      <header className="top">
        <div>
          <h1>Monitor de microservicios</h1>
          <p className="live" role="status">
            <span className={`dot ${error ? 'dot-off' : ''}`} aria-hidden="true" />
            {error
              ? 'Sin conexión con el backend, reintentando'
              : updatedAt
                ? `En vivo · actualizado ${updatedAt.toLocaleTimeString('es', { hour12: false })} · cada 3 s`
                : 'Conectando…'}
          </p>
        </div>
        <div className="top-actions">
          {summary && (
            <p className="overall">
              <strong>{summary.totalCalls.toLocaleString('es')}</strong> llamadas ·{' '}
              <strong className={summary.errorRate > 15 ? 'err-text' : ''}>{summary.errorRate}%</strong> de error
            </p>
          )}
          <button className="btn" onClick={onSimulate} disabled={loading}>
            {loading ? <SpinnerIcon /> : <BoltIcon />}
            {loading ? 'Simulando…' : 'Simular carga'}
          </button>
        </div>
      </header>

      {error && (
        <div className="banner banner-bad" role="alert">
          No se pudo conectar con el backend ({error}). Verifica que esté en ejecución y que <code>VITE_API_URL</code> apunte a él.
        </div>
      )}
      {notice && <div className="banner banner-info" role="status">{notice}</div>}

      <section className="svc-grid" aria-label="Estado de los servicios">
        {summary
          ? summary.services.map((s) => <ServiceCard key={s.serviceId} summary={s} />)
          : SERVICES.map((id) => <div key={id} className="svc skeleton" aria-hidden="true" />)}
      </section>

      <section className="panel">
        <h2>Tiempo de respuesta, últimas 20 llamadas</h2>
        <LatencyChart series={series} />
      </section>

      <section className="panel">
        <h2>Logs</h2>
        <Filters filters={filters} onChange={changeFilters} />
        <LogsTable page={page} onPage={setPageNum} filtered={filtered} />
      </section>
    </div>
  )
}
