import { Fragment, useState } from 'react'
import { ChevronIcon } from './icons'
import { SERVICE_META } from './ServiceCard'

const json = (v) => (v == null ? '—' : JSON.stringify(v, null, 2))

export default function LogsTable({ page, onPage, filtered }) {
  const [open, setOpen] = useState(null)
  const toggle = (id) => setOpen((cur) => (cur === id ? null : id))

  return (
    <>
      <div className="table-scroll">
        <table className="logs">
          <thead>
            <tr>
              <th className="col-chev"><span className="sr-only">Detalle</span></th>
              <th>Request ID</th>
              <th>Servicio</th>
              <th>Operación</th>
              <th className="num">Duración</th>
              <th>Estado</th>
              <th>Hora</th>
            </tr>
          </thead>
          <tbody>
            {page.content.map((l) => {
              const isOpen = open === l.requestId
              const error = l.status === 'ERROR'
              return (
                <Fragment key={l.requestId}>
                  <tr
                    className={`row ${isOpen ? 'row-open' : ''} ${error ? 'row-err' : ''}`}
                    tabIndex={0}
                    aria-expanded={isOpen}
                    onClick={() => toggle(l.requestId)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggle(l.requestId) }
                    }}
                  >
                    <td className="col-chev"><ChevronIcon className={`chev ${isOpen ? 'chev-open' : ''}`} width={16} height={16} /></td>
                    <td className="mono">{l.requestId.slice(0, 8)}</td>
                    <td>
                      <span className="swatch" style={{ background: SERVICE_META[l.serviceId]?.color }} aria-hidden="true" />
                      {SERVICE_META[l.serviceId]?.label || l.serviceId}
                    </td>
                    <td className="mono">{l.operation}</td>
                    <td className="num mono">{l.durationMs} ms</td>
                    <td><span className={`chip ${error ? 'chip-bad' : 'chip-ok'}`}>{l.status}</span></td>
                    <td className="mono muted">{new Date(l.timestamp).toLocaleTimeString('es', { hour12: false })}</td>
                  </tr>
                  {isOpen && (
                    <tr className="detail">
                      <td colSpan={7}>
                        <div className="detail-grid">
                          <section>
                            <h4>Identificador</h4>
                            <pre>{l.requestId}{'\n'}{new Date(l.timestamp).toLocaleString('es')}</pre>
                          </section>
                          <section>
                            <h4>Parámetros de entrada</h4>
                            <pre>{json(l.params)}</pre>
                          </section>
                          <section>
                            <h4>{error ? 'Error' : 'Respuesta completa'}</h4>
                            <pre className={error ? 'pre-err' : ''}>{error ? l.error : json(l.response)}</pre>
                          </section>
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              )
            })}
            {page.content.length === 0 && (
              <tr>
                <td colSpan={7} className="empty">
                  {filtered ? 'Ningún log coincide con los filtros.' : 'Todavía no hay logs. Pulsa «Simular carga» para generar tráfico.'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      <nav className="pager" aria-label="Paginación">
        <button className="btn-secondary" disabled={page.page <= 0} onClick={() => onPage(page.page - 1)}>Anterior</button>
        <span>Página {page.totalPages === 0 ? 0 : page.page + 1} de {page.totalPages} · {page.totalElements.toLocaleString('es')} logs</span>
        <button className="btn-secondary" disabled={page.page + 1 >= page.totalPages} onClick={() => onPage(page.page + 1)}>Siguiente</button>
      </nav>
    </>
  )
}
