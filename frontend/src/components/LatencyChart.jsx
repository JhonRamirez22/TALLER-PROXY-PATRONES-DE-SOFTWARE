import { useMemo } from 'react'
import { Line } from 'react-chartjs-2'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend,
} from 'chart.js'
import { SERVICE_META } from './ServiceCard'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend)

const css = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim()

/** series: { serviceId: [LogEntry...] }, las últimas 20 llamadas, más reciente primero. */
export default function LatencyChart({ series }) {
  const { data, options } = useMemo(() => {
    const ordered = Object.fromEntries(Object.entries(series).map(([id, logs]) => [id, [...logs].reverse()]))
    const bad = css('--bad')
    const font = { family: "'IBM Plex Sans', system-ui, sans-serif", size: 12 }
    const ink = css('--ink-2')

    const data = {
      labels: Array.from({ length: 20 }, (_, i) => i + 1),
      datasets: Object.entries(ordered).map(([id, logs]) => {
        const color = css(`--svc-${id}`)
        return {
          label: SERVICE_META[id]?.label || id,
          data: logs.map((l) => l.durationMs),
          borderColor: color,
          backgroundColor: color,
          borderWidth: 2,
          tension: 0.25,
          // las llamadas fallidas se marcan con triángulo rojo (forma + color)
          pointStyle: logs.map((l) => (l.status === 'ERROR' ? 'triangle' : 'circle')),
          pointRadius: logs.map((l) => (l.status === 'ERROR' ? 7 : 3)),
          pointBackgroundColor: logs.map((l) => (l.status === 'ERROR' ? bad : color)),
          pointBorderColor: logs.map((l) => (l.status === 'ERROR' ? bad : color)),
          pointHoverRadius: 7,
        }
      }),
    }

    const options = {
      animation: false,
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'nearest', intersect: false },
      plugins: {
        legend: { position: 'top', align: 'start', labels: {
            usePointStyle: true, pointStyle: 'rectRounded', boxWidth: 10, boxHeight: 10, color: ink, font,
            // la leyenda no hereda los triángulos de error de la primera muestra
            generateLabels: (chart) => chart.data.datasets.map((d, i) => ({
              text: d.label, fillStyle: d.borderColor, strokeStyle: d.borderColor, pointStyle: 'rectRounded',
              hidden: !chart.isDatasetVisible(i), datasetIndex: i,
            })),
          } },
        tooltip: {
          titleFont: font, bodyFont: font,
          callbacks: {
            title: (items) => {
              const it = items[0]
              const l = ordered[Object.keys(ordered)[it.datasetIndex]]?.[it.dataIndex]
              return l ? `${l.serviceId} · ${l.operation}` : ''
            },
            label: (ctx) => {
              const l = ordered[Object.keys(ordered)[ctx.datasetIndex]]?.[ctx.dataIndex]
              return `${ctx.parsed.y} ms${l?.status === 'ERROR' ? ' · ERROR' : ''}`
            },
          },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: ink, font },
          title: { display: true, text: 'Llamada (de la más antigua a la más reciente)', color: ink, font },
        },
        y: {
          beginAtZero: true,
          grid: { color: css('--line') },
          ticks: { color: ink, font, callback: (v) => `${v} ms` },
        },
      },
    }
    return { data, options }
  }, [series])

  const empty = Object.values(series).every((l) => l.length === 0)

  return (
    <div className="chart-wrap">
      <div className="chart-box">
        <Line data={data} options={options} aria-label="Tiempo de respuesta de las últimas 20 llamadas por servicio" role="img" />
        {empty && <p className="chart-empty">Aún no hay llamadas. Pulsa «Simular carga».</p>}
      </div>
      <p className="legend-note">
        <svg width="12" height="12" viewBox="0 0 12 12" aria-hidden="true"><path d="M6 1 11 10H1L6 1Z" fill="var(--bad)" /></svg>
        Triángulo rojo: llamada fallida
      </p>
    </div>
  )
}
