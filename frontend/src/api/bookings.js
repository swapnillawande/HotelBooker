import { apiRequest } from './client'

export function initialiseBooking(request) {
  return apiRequest('/bookings/init', { method: 'POST', body: JSON.stringify(request) })
}

const bookingHeaders = (managementToken) => ({ 'X-Booking-Token': managementToken })

export function addBookingGuests(bookingId, managementToken, guests) {
  return apiRequest(`/bookings/${bookingId}/addGuests`, { method: 'POST', headers: bookingHeaders(managementToken), body: JSON.stringify(guests) })
}

export function confirmBooking(bookingId, managementToken) {
  return apiRequest(`/bookings/${bookingId}/confirm`, { method: 'POST', headers: bookingHeaders(managementToken) })
}

export function getBooking(bookingId, managementToken) {
  return apiRequest(`/bookings/${bookingId}`, { headers: bookingHeaders(managementToken) })
}

export function cancelBooking(bookingId, managementToken) {
  return apiRequest(`/bookings/${bookingId}/cancel`, { method: 'POST', headers: bookingHeaders(managementToken) })
}
