import { apiRequest } from './client'

export function loginAccount(credentials) {
  return apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(credentials) })
}

export function registerAccount(details) {
  return apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(details) })
}

export function getCurrentUser() {
  return apiRequest('/auth/me')
}

export function logoutAccount() {
  return apiRequest('/auth/logout', { method: 'POST' })
}

export function getAccountBookings() {
  return apiRequest('/account/bookings')
}

export function cancelAccountBooking(bookingId) {
  return apiRequest(`/account/bookings/${bookingId}/cancel`, { method: 'POST' })
}
