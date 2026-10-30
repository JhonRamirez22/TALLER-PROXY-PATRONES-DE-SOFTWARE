const BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

async function request(path, options) {
  const res = await fetch(`${BASE}${path}`, options)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

export const getSummary = () => request('/api/metrics/summary')

export const getLogs = (params) => {
  const qs = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== '' && v != null) qs.set(k, v)
  })
  return request(`/api/metrics/logs?${qs}`)
}

export const simulateLoad = () => request('/api/metrics/simulate-load', { method: 'POST' })
