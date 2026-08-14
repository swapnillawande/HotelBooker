import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { addBookingGuests, initialiseBooking } from '../api/bookings'
import { demoHotels } from '../data/demoHotels'

export default function BookingPage() {
  const [params] = useSearchParams()
  const hotel = demoHotels.find((item) => String(item.id) === params.get('hotelId')) || demoHotels[0]
  const room = hotel.rooms.find((item) => String(item.id) === params.get('roomId')) || hotel.rooms[0]
  const guestCount = Number(params.get('guests') || 2)
  const [guests, setGuests] = useState(Array.from({ length: guestCount }, (_, index) => ({ name: '', age: '', gender: 'OTHER', key: index })))
  const [status, setStatus] = useState('idle')
  const [message, setMessage] = useState('')
  const nights = useMemo(() => Math.max(1, Math.ceil((new Date(params.get('checkOut')) - new Date(params.get('checkIn'))) / 86400000)), [params])
  const total = Number(room.basePrice) * nights * Number(params.get('rooms') || 1)

  const updateGuest = (index, field, value) => setGuests((current) => current.map((guest, guestIndex) => guestIndex === index ? { ...guest, [field]: value } : guest))
  const submit = async (event) => {
    event.preventDefault()
    setStatus('loading')
    try {
      const booking = await initialiseBooking({ hotelId: Number(params.get('hotelId')), roomId: Number(params.get('roomId')), checkInDate: params.get('checkIn'), checkOutDate: params.get('checkOut'), roomsCount: Number(params.get('rooms') || 1) })
      await addBookingGuests(booking.id, guests.map(({ key, ...guest }) => ({ ...guest, age: Number(guest.age) })))
      setMessage(`Booking #${booking.id} is reserved and all guests were added.`)
      setStatus('success')
    } catch (error) {
      setMessage(`${error.message}. The UI flow is ready; seed matching inventory to complete a live reservation.`)
      setStatus('error')
    }
  }

  if (status === 'success') return <main className="booking-page"><section className="success-card"><span>✓</span><h1>You’re booked!</h1><p>{message}</p><Link className="primary-button" to="/">Back to stays</Link></section></main>

  return <main className="booking-page"><div className="booking-layout"><form className="guest-form" onSubmit={submit}><span className="eyebrow">Final step</span><h1>Who’s checking in?</h1><p>Add the travellers staying in this room. The backend holds availability for ten minutes after initialization.</p>{guests.map((guest, index) => <fieldset key={guest.key}><legend>Guest {index + 1}</legend><label>Full name<input required value={guest.name} onChange={(event) => updateGuest(index, 'name', event.target.value)} placeholder="Enter full name" /></label><div className="form-row"><label>Age<input required min="1" max="120" type="number" value={guest.age} onChange={(event) => updateGuest(index, 'age', event.target.value)} /></label><label>Gender<select value={guest.gender} onChange={(event) => updateGuest(index, 'gender', event.target.value)}><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="OTHER">Other</option></select></label></div></fieldset>)}{status === 'error' && <div className="form-message error">{message}</div>}<button className="primary-button confirm-button" disabled={status === 'loading'}>{status === 'loading' ? 'Reserving…' : `Reserve for €${total}`}</button></form><aside className="booking-summary"><img src={hotel.image} alt="" /><span className="eyebrow">Your selection</span><h2>{hotel.name}</h2><p>{room.type}</p><dl><div><dt>Check in</dt><dd>{params.get('checkIn')}</dd></div><div><dt>Check out</dt><dd>{params.get('checkOut')}</dd></div><div><dt>Stay</dt><dd>{nights} night{nights > 1 ? 's' : ''}</dd></div><div><dt>Guests</dt><dd>{guestCount}</dd></div></dl><div className="summary-total"><span>Total</span><strong>€{total}</strong></div></aside></div></main>
}
