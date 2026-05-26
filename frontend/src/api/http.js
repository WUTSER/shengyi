import { API_BASE } from '../config'

export async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  })
  const payload = await response.json().catch(() => ({}))
  if (!response.ok || payload.code !== '0') {
    throw new Error(payload.message || `HTTP ${response.status}`)
  }
  return payload.data
}
