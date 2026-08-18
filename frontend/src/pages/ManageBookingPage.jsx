import { useState } from 'react'
import { cancelBooking, getBooking } from '../api/bookings'

function savedAccess() {
  try {
    return JSON.parse(sessionStorage.getItem('stayly-booking-access')) || {}
  } catch {
    return {}
  }
}

export default function ManageBookingPage() {
  const saved = savedAccess()
  const [bookingId, setBookingId] = useState(saved.id || '')
  const [managementToken, setManagementToken] = useState(saved.managementToken || '')
  const [booking, setBooking] = useState(null)
  const [status, setStatus] = useState('idle')
  const [message, setMessage] = useState('')

  const lookup = async (event) => {
    event.preventDefault()
    setStatus('loading')
    setMessage('')
    try {
      const result = await getBooking(Number(bookingId), managementToken.trim())
      setBooking(result)
      sessionStorage.setItem('stayly-booking-access', JSON.stringify({ id: result.id, managementToken: result.managementToken }))
      setStatus('success')
    } catch (error) {
      setBooking(null)
      setMessage(error.message)
      setStatus('error')
    }
  }

  const cancel = async () => {
    if (!window.confirm(`Cancel booking #${booking.id}? The reserved rooms will be released.`)) return
    setStatus('cancelling')
    setMessage('')
    try {
      const result = await cancelBooking(booking.id, managementToken.trim())
      setBooking(result)
      setMessage(`Booking #${result.id} has been cancelled and its rooms were released.`)
      setStatus('success')
    } catch (error) {
      setMessage(error.message)
      setStatus('error')
    }
  }

  const canCancel = booking && !['CANCELLED', 'EXPIRED'].includes(booking.bookingStatus) && new Date(`${booking.checkInDate}T00:00:00`) > new Date()

  return (
    <main className="manage-booking-page">
      <section className="manage-booking-intro">
        <span className="eyebrow">Your stay</span>
        <h1>View or cancel a booking.</h1>
        <p>Enter the booking number and private access code from your confirmation.</p>
      </section>
      <section className="manage-booking-shell">
        <form className="booking-lookup" onSubmit={lookup}>
          <label>Booking number<input required min="1" type="number" value={bookingId} onChange={(event) => setBookingId(event.target.value)} placeholder="e.g. 42" /></label>
          <label>Private access code<input required type="password" autoComplete="off" value={managementToken} onChange={(event) => setManagementToken(event.target.value)} placeholder="36-character code" /></label>
          <button className="primary-button" disabled={status === 'loading'}>{status === 'loading' ? 'Finding booking…' : 'Find booking'}</button>
        </form>
        {status === 'error' && <div className="form-message error" role="alert">{message}</div>}
        {message && status === 'success' && <div className="form-message success" role="status">{message}</div>}
        {booking && <article className="managed-booking-card">
          <div className="managed-booking-heading"><div><span className="eyebrow">Booking #{booking.id}</span><h2>Hotel #{booking.hotelId}</h2></div><span className={`status-chip ${booking.bookingStatus === 'CONFIRMED' ? 'active' : ''}`}>{booking.bookingStatus.replace('_', ' ')}</span></div>
          <dl>
            <div><dt>Check in</dt><dd>{booking.checkInDate}</dd></div>
            <div><dt>Check out</dt><dd>{booking.checkOutDate}</dd></div>
            <div><dt>Rooms</dt><dd>{booking.roomsCount}</dd></div>
            <div><dt>Total</dt><dd>€{booking.amount}</dd></div>
          </dl>
          <div className="managed-guests"><strong>Guests</strong><span>{booking.guests?.map((guest) => guest.name).join(', ') || 'Not added yet'}</span></div>
          {booking.paymentStatus && <div className="managed-payment"><strong>Payment</strong><span>{booking.paymentStatus.replace('_', ' ')}{booking.paymentReference ? ` · Ref ${booking.paymentReference}` : ''}</span></div>}
          {canCancel ? <button className="danger-button" disabled={status === 'cancelling'} onClick={cancel}>{status === 'cancelling' ? 'Cancelling…' : 'Cancel booking'}</button> : <p className="cancellation-note">This booking can no longer be cancelled online.</p>}
        </article>}
      </section>
    </main>
  )
}
