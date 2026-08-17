import { apiRequest } from './client'

export function initialiseBooking(request) {
  return apiRequest('/bookings/init', { method: 'POST', body: JSON.stringify(request) })
}

export function addBookingGuests(bookingId, guests) {
  return apiRequest(`/bookings/${bookingId}/addGuests`, { method: 'POST', body: JSON.stringify(guests) })
}

export function confirmBooking(bookingId) {
  return apiRequest(`/bookings/${bookingId}/confirm`, { method: 'POST' })
}
