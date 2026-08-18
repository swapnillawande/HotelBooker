import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { cancelAccountBooking, getAccountBookings } from '../api/auth'
import { useAuth } from '../auth/AuthContext'
import { demoHotels } from '../data/demoHotels'

const hotelFor = (hotelId) => demoHotels.find((hotel) => Number(hotel.id) === Number(hotelId))
const canCancel = (booking) => !['CANCELLED', 'EXPIRED'].includes(booking.bookingStatus)
  && new Date(`${booking.checkInDate}T00:00:00`) > new Date()

export default function MyBookingsPage() {
  const { user } = useAuth()
  const [bookings, setBookings] = useState([])
  const [status, setStatus] = useState('loading')
  const [message, setMessage] = useState('')
  const [cancellingId, setCancellingId] = useState(null)

  useEffect(() => {
    let active = true
    getAccountBookings()
      .then((result) => active && setBookings(result))
      .catch((error) => {
        if (!active) return
        setMessage(error.message)
        setStatus('error')
      })
      .finally(() => active && setStatus((current) => current === 'error' ? current : 'success'))
    return () => { active = false }
  }, [])

  const cancel = async (booking) => {
    if (!window.confirm(`Cancel booking #${booking.id}? Its reserved rooms will be released.`)) return
    setCancellingId(booking.id)
    setMessage('')
    try {
      const updated = await cancelAccountBooking(booking.id)
      setBookings((current) => current.map((item) => item.id === updated.id ? updated : item))
      setMessage(`Booking #${updated.id} has been cancelled.`)
    } catch (error) {
      setMessage(error.message)
    } finally {
      setCancellingId(null)
    }
  }

  return (
    <main className="trips-page">
      <section className="trips-header"><div><span className="eyebrow">Your Stayly account</span><h1>Your bookings.</h1><p>Welcome back, {user.name}. Review every trip connected to {user.email}.</p></div><Link className="primary-button" to="/">Find another stay</Link></section>
      <section className="trips-shell">
        {status === 'loading' && <div className="trips-empty">Loading your bookings…</div>}
        {status === 'error' && <div className="form-message error" role="alert">{message}</div>}
        {message && status !== 'error' && <div className={`form-message ${message.includes('cancelled') ? 'success' : 'error'}`} role="status">{message}</div>}
        {status === 'success' && bookings.length === 0 && <div className="trips-empty"><span>⌂</span><h2>No bookings yet</h2><p>Your next city break will appear here when you book while signed in.</p><Link className="outline-button" to="/">Explore stays</Link></div>}
        <div className="trip-list">
          {bookings.map((booking) => {
            const hotel = hotelFor(booking.hotelId)
            return <article className="trip-card" key={booking.id}>
              <div className="trip-image" style={hotel?.image ? { backgroundImage: `url(${hotel.image})` } : undefined}><span>Booking #{booking.id}</span></div>
              <div className="trip-content">
                <div className="trip-heading"><div><span className="eyebrow">{hotel?.city || `Hotel #${booking.hotelId}`}</span><h2>{hotel?.name || `Stayly hotel #${booking.hotelId}`}</h2></div><span className={`status-chip ${booking.bookingStatus === 'CONFIRMED' ? 'active' : ''}`}>{booking.bookingStatus.replace('_', ' ')}</span></div>
                <dl><div><dt>Check in</dt><dd>{booking.checkInDate}</dd></div><div><dt>Check out</dt><dd>{booking.checkOutDate}</dd></div><div><dt>Rooms</dt><dd>{booking.roomsCount}</dd></div><div><dt>Total</dt><dd>€{booking.amount}</dd></div></dl>
                <p className="trip-guests"><strong>Guests:</strong> {booking.guests?.map((guest) => guest.name).join(', ') || 'Not added yet'}</p>
                {booking.paymentStatus && <p className="trip-payment"><strong>Payment:</strong> {booking.paymentStatus.replace('_', ' ')}{booking.paymentReference ? ` · ${booking.paymentReference}` : ''}</p>}
                {canCancel(booking) ? <button className="danger-button" disabled={cancellingId === booking.id} onClick={() => cancel(booking)}>{cancellingId === booking.id ? 'Cancelling…' : 'Cancel booking'}</button> : <p className="cancellation-note">This booking can no longer be cancelled online.</p>}
              </div>
            </article>
          })}
        </div>
      </section>
    </main>
  )
}
