import { request } from './http'

export function listReimbursements(params) {
  return request(`/travel-reimbursements?${params}`)
}

export function getReimbursement(reimbursementId) {
  return request(`/travel-reimbursements/${reimbursementId}`)
}

export function createReimbursement(payload) {
  return request('/travel-reimbursements', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateReimbursement(reimbursementId, payload) {
  return request(`/travel-reimbursements/${reimbursementId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteReimbursement(reimbursementId) {
  return request(`/travel-reimbursements/${reimbursementId}`, { method: 'DELETE' })
}

export function submitReimbursement(reimbursementId) {
  return request(`/travel-reimbursements/${reimbursementId}/submit`, { method: 'POST' })
}

export function createTrip(reimbursementId, payload) {
  return request(`/travel-reimbursements/${reimbursementId}/trips`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function updateTrip(reimbursementId, tripId, payload) {
  return request(`/travel-reimbursements/${reimbursementId}/trips/${tripId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export function deleteTrip(reimbursementId, tripId) {
  return request(`/travel-reimbursements/${reimbursementId}/trips/${tripId}`, { method: 'DELETE' })
}

export function getAllowanceCalendar(reimbursementId, allowanceId) {
  return request(`/travel-reimbursements/${reimbursementId}/allowances/${allowanceId}/calendar`)
}

export function saveAllowanceCalendar(reimbursementId, allowanceId, payload) {
  return request(`/travel-reimbursements/${reimbursementId}/allowances/${allowanceId}/calendar`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}
