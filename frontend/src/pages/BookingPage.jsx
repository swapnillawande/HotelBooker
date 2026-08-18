import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { addBookingGuests, initialiseBooking, payBooking } from '../api/bookings'
import { useAuth } from '../auth/AuthContext'
import { demoHotels } from '../data/demoHotels'

const createIdempotencyKey = () => globalThis.crypto?.randomUUID?.()
  || `checkout-${Date.now()}-${Math.random().toString(16).slice(2)}`

const formatCardNumber = (value) => value.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim()
const formatExpiry = (value) => {
  const digits = value.replace(/\D/g, '').slice(0, 4)
  return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits
}

export default function BookingPage() {
  const { user } = useAuth()
  const [params] = useSearchParams()
  const hotel = demoHotels.find((item) => String(item.id) === params.get('hotelId')) || demoHotels[0]
  const room = hotel.rooms.find((item) => String(item.id) === params.get('roomId')) || hotel.rooms[0]
  const hotelName = params.get('hotelName') || hotel.name
  const roomType = params.get('roomType') || room.type
  const guestCount = Number(params.get('guests') || 2)
  const [guests, setGuests] = useState(Array.from({ length: guestCount }, (_, index) => ({ name: '', age: '', gender: 'OTHER', key: index })))
  const [payment, setPayment] = useState({ cardholderName: user?.name || '', cardNumber: '4242 4242 4242 4242', expiry: '12/30', cvc: '123' })
  const [idempotencyKey] = useState(createIdempotencyKey)
  const [status, setStatus] = useState('idle')
  const [message, setMessage] = useState('')
  const [booking, setBooking] = useState(null)
  const nights = useMemo(() => Math.max(1, Math.ceil((new Date(params.get('checkOut')) - new Date(params.get('checkIn'))) / 86400000)), [params])
  const quotedTotal = Number(params.get('totalPrice'))
  const total = Number.isFinite(quotedTotal) && quotedTotal > 0 ? quotedTotal : Number(room.basePrice) * nights * Number(params.get('rooms') || 1)
  const paymentStep = booking?.bookingStatus === 'GUEST_ADDED'

  const updateGuest = (index, field, value) => setGuests((current) => current.map((guest, guestIndex) => guestIndex === index ? { ...guest, [field]: value } : guest))
  const updatePayment = (field, value) => setPayment((current) => ({ ...current, [field]: value }))

  const submit = async (event) => {
    event.preventDefault()
    setStatus('loading')
    setMessage('')
    let activeBooking = booking

    try {
      if (!activeBooking) {
        activeBooking = await initialiseBooking({
          hotelId: Number(params.get('hotelId')),
          roomId: Number(params.get('roomId')),
          checkInDate: params.get('checkIn'),
          checkOutDate: params.get('checkOut'),
          roomsCount: Number(params.get('rooms') || 1),
        })
        setBooking(activeBooking)
      }

      if (activeBooking.bookingStatus === 'RESERVED') {
        activeBooking = await addBookingGuests(
          activeBooking.id,
          activeBooking.managementToken,
          guests.map(({ key, ...guest }) => ({ ...guest, age: Number(guest.age) })),
        )
        setBooking(activeBooking)
        setStatus('payment')
        return
      }

      if (activeBooking.bookingStatus === 'GUEST_ADDED') {
        if (payment.cardNumber.replace(/\D/g, '') !== '4242424242424242') {
          throw new Error('Use the demo Visa number 4242 4242 4242 4242')
        }
        activeBooking = await payBooking(
          activeBooking.id,
          activeBooking.managementToken,
          idempotencyKey,
          payment.cardholderName.trim(),
        )
        setBooking(activeBooking)
      }

      sessionStorage.setItem('stayly-booking-access', JSON.stringify({ id: activeBooking.id, managementToken: activeBooking.managementToken }))
      setMessage(`Booking #${activeBooking.id} is confirmed and payment is complete.`)
      setStatus('success')
    } catch (error) {
      setMessage(error.message)
      setStatus('error')
    }
  }

  if (status === 'success') {
    return <main className="booking-page"><section className="success-card"><span>✓</span><h1>You’re booked!</h1><p>{message}</p><div className="payment-confirmation"><small>Payment confirmed</small><strong>€{booking.amount}</strong><code>{booking.paymentReference}</code></div>{user ? <div className="booking-account-note"><strong>Saved to your account</strong><p>This trip is now available in My Bookings—no access code needed.</p></div> : <div className="booking-access-code"><small>Private booking access code</small><code>{booking.managementToken}</code><p>Keep this code with booking #{booking.id}. You will need both to view or cancel the booking.</p></div>}<div className="success-actions"><Link className="primary-button" to={user ? '/my-bookings' : '/manage-booking'}>{user ? 'View my bookings' : 'Manage booking'}</Link><Link className="outline-button" to="/">Back to stays</Link></div></section></main>
  }

  return (
    <main className="booking-page">
      <div className="checkout-steps" aria-label="Checkout progress"><span className="complete">1 · Stay</span><span className={!paymentStep ? 'active' : 'complete'}>2 · Guests</span><span className={paymentStep ? 'active' : ''}>3 · Payment</span></div>
      <div className="booking-layout">
        <form className="guest-form" onSubmit={submit}>
          <span className="eyebrow">{paymentStep ? 'Secure demo checkout' : 'Guest details'}</span>
          <h1>{paymentStep ? 'Complete payment.' : 'Who’s checking in?'}</h1>
          <p>{paymentStep ? 'Your rooms remain held while you complete this demo payment.' : 'Add the travellers staying in this room. Availability is held for ten minutes after initialization.'}</p>
          {user && <div className="signed-in-booking">Booking as <strong>{user.name}</strong> · This stay will appear in My Bookings.</div>}

          {!paymentStep && guests.map((guest, index) => <fieldset key={guest.key}><legend>Guest {index + 1}</legend><label>Full name<input required value={guest.name} onChange={(event) => updateGuest(index, 'name', event.target.value)} placeholder="Enter full name" /></label><div className="form-row"><label>Age<input required min="1" max="120" type="number" value={guest.age} onChange={(event) => updateGuest(index, 'age', event.target.value)} /></label><label>Gender<select value={guest.gender} onChange={(event) => updateGuest(index, 'gender', event.target.value)}><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="OTHER">Other</option></select></label></div></fieldset>)}

          {paymentStep && <div className="payment-form"><div className="demo-payment-banner"><strong>Demo payment</strong><span>No real charge is made. The card number, expiry, and CVC never leave this page.</span></div><label>Cardholder name<input required autoComplete="cc-name" value={payment.cardholderName} onChange={(event) => updatePayment('cardholderName', event.target.value)} placeholder="Name on card" /></label><label>Card number<input required inputMode="numeric" autoComplete="cc-number" value={payment.cardNumber} onChange={(event) => updatePayment('cardNumber', formatCardNumber(event.target.value))} pattern="[0-9 ]{19}" /></label><div className="form-row"><label>Expiry<input required inputMode="numeric" autoComplete="cc-exp" value={payment.expiry} onChange={(event) => updatePayment('expiry', formatExpiry(event.target.value))} pattern="(0[1-9]|1[0-2])/[0-9]{2}" /></label><label>CVC<input required inputMode="numeric" autoComplete="cc-csc" value={payment.cvc} onChange={(event) => updatePayment('cvc', event.target.value.replace(/\D/g, '').slice(0, 3))} pattern="[0-9]{3}" type="password" /></label></div><div className="secure-payment-note">🔒 Idempotent checkout protects against duplicate charges when retrying.</div></div>}

          {status === 'error' && <div className="form-message error" role="alert">{message}</div>}
          <button className="primary-button confirm-button" disabled={status === 'loading'}>{status === 'loading' ? (paymentStep ? 'Processing payment…' : booking ? 'Saving guests…' : 'Holding your room…') : paymentStep ? `Pay €${booking.amount}` : booking ? 'Continue to payment' : `Reserve & continue · €${total}`}</button>
        </form>

        <aside className="booking-summary"><img src={hotel.image} alt="" /><span className="eyebrow">Your selection</span><h2>{hotelName}</h2><p>{roomType}</p><dl><div><dt>Check in</dt><dd>{params.get('checkIn')}</dd></div><div><dt>Check out</dt><dd>{params.get('checkOut')}</dd></div><div><dt>Stay</dt><dd>{nights} night{nights > 1 ? 's' : ''}</dd></div><div><dt>Guests</dt><dd>{guestCount}</dd></div><div><dt>Rooms</dt><dd>{params.get('rooms') || 1}</dd></div></dl><div className="summary-total"><span>Total</span><strong>€{booking?.amount ?? total}</strong></div>{paymentStep && <small className="summary-payment-note">Payment due now · Demo Visa ending 4242</small>}</aside>
      </div>
    </main>
  )
}
