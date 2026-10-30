const EMPTY = { service: '', status: '', from: '', to: '' }

export default function Filters({ filters, onChange }) {
  const set = (k) => (e) => onChange({ ...filters, [k]: e.target.value })
  const active = Object.values(filters).some(Boolean)
  return (
    <form className="filters" onSubmit={(e) => e.preventDefault()} aria-label="Filtros de logs">
      <label>
        <span>Servicio</span>
        <select value={filters.service} onChange={set('service')}>
          <option value="">Todos</option>
          <option value="inventory">Inventario</option>
          <option value="orders">Pedidos</option>
          <option value="payments">Pagos</option>
        </select>
      </label>
      <label>
        <span>Estado</span>
        <select value={filters.status} onChange={set('status')}>
          <option value="">Todos</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="ERROR">ERROR</option>
        </select>
      </label>
      <label>
        <span>Desde</span>
        <input type="datetime-local" value={filters.from} max={filters.to || undefined} onChange={set('from')} />
      </label>
      <label>
        <span>Hasta</span>
        <input type="datetime-local" value={filters.to} min={filters.from || undefined} onChange={set('to')} />
      </label>
      {active && (
        <button type="button" className="btn-text" onClick={() => onChange(EMPTY)}>Quitar filtros</button>
      )}
    </form>
  )
}
