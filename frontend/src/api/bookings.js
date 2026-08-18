import { apiRequest } from './client'

export function initialiseBooking(request) {
  return apiRequest('/bookings/init', { method: 'POST', body: JSON.stringify(request) })
}

const bookingHeaders = (managementToken) => ({ 'X-Booking-Token': managementToken })

export function addBookingGuests(bookingId, managementToken, guests) {
  return apiRequest(`/bookings/${bookingId}/addGuests`, { method: 'POST', headers: bookingHeaders(managementToken), body: JSON.stringify(guests) })
}

export function payBooking(bookingId, managementToken, idempotencyKey, cardholderName) {
  return apiRequest(`/bookings/${bookingId}/pay`, {
    method: 'POST',
    headers: { ...bookingHeaders(managementToken), 'Idempotency-Key': idempotencyKey },
    body: JSON.stringify({ paymentToken: 'tok_demo_visa', cardholderName }),
  })
}

export function getBooking(bookingId, managementToken) {
  return apiRequest(`/bookings/${bookingId}`, { headers: bookingHeaders(managementToken) })
}

export function cancelBooking(bookingId, managementToken) {
  return apiRequest(`/bookings/${bookingId}/cancel`, { method: 'POST', headers: bookingHeaders(managementToken) })
}
