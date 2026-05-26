export function formatDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

export function formatRange(start, end) {
  if (!start || !end) return ''
  return `${start} 至 ${end}`
}

export function formatMoney(value) {
  const numeric = Number(value || 0)
  return numeric.toFixed(2)
}

export function parsePercent(value) {
  const numeric = Number(String(value || '').replace('%', '').trim())
  if (!Number.isFinite(numeric)) return 0
  return roundRatio(Math.min(Math.max(numeric, 0), 100) / 100)
}

export function formatPercent(value) {
  return (Number(value || 0) * 100).toFixed(2)
}

export function roundRatio(value) {
  return Math.round(Number(value || 0) * 10000) / 10000
}

export function roundMoney(value) {
  return Math.round(Number(value || 0) * 100) / 100
}
