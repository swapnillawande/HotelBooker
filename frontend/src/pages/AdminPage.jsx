import { useEffect, useMemo, useState } from 'react'
import {
  activateHotel,
  createHotel,
  createRoom,
  deleteHotel,
  deleteRoom,
  getAdminHotels,
  getHotelRooms,
  updateHotel,
} from '../api/hotels'
import { getManagerBookingDashboard } from '../api/bookings'

const emptyHotel = { name: '', city: '', amenities: [], photos: [], contactInfo: {} }
const emptyRoom = { type: '', basePrice: '', totalCount: '', capacity: '', amenities: [], photos: [] }
const emptyDashboard = { totalBookings: 0, confirmedBookings: 0, arrivalsNextSevenDays: 0, confirmedRevenue: 0, bookings: [] }

export default function AdminPage() {
  const [portfolio, setPortfolio] = useState([])
  const [hotel, setHotel] = useState(emptyHotel)
  const [rooms, setRooms] = useState([])
  const [room, setRoom] = useState(emptyRoom)
  const [loading, setLoading] = useState(true)
  const [notice, setNotice] = useState({ type: '', text: '' })
  const [dashboard, setDashboard] = useState(emptyDashboard)
  const [bookingProperty, setBookingProperty] = useState('all')
  const [bookingStatus, setBookingStatus] = useState('all')

  useEffect(() => {
    let cancelled = false
    Promise.all([getAdminHotels(), getManagerBookingDashboard()])
      .then(async ([properties, bookingDashboard]) => {
        if (cancelled) return
        setPortfolio(properties)
        setDashboard(bookingDashboard)
        if (properties.length > 0) {
          const first = properties[0]
          setHotel(first)
          setRooms(await getHotelRooms(first.id))
        }
      })
      .catch((error) => !cancelled && setNotice({ type: 'error', text: error.message }))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [])

  const changeHotel = (event) => setHotel({
    ...hotel,
    [event.target.name]: event.target.name === 'amenities'
      ? event.target.value.split(',').map((item) => item.trim()).filter(Boolean)
      : event.target.value,
  })
  const changeRoom = (event) => setRoom({
    ...room,
    [event.target.name]: event.target.name === 'amenities'
      ? event.target.value.split(',').map((item) => item.trim()).filter(Boolean)
      : event.target.value,
  })
  const run = async (action, success) => {
    try {
      const data = await action()
      setNotice({ type: 'success', text: success })
      return data
    } catch (error) {
      setNotice({ type: 'error', text: error.message })
      return false
    }
  }
  const openProperty = async (property) => {
    setHotel(property)
    setRooms([])
    setNotice({ type: '', text: '' })
    const data = await run(() => getHotelRooms(property.id), `${property.name} workspace loaded.`)
    if (data !== false) setRooms(data)
  }
  const startNew = () => {
    setHotel(emptyHotel)
    setRooms([])
    setRoom(emptyRoom)
    setNotice({ type: '', text: '' })
  }
  const save = async (event) => {
    event.preventDefault()
    const data = hotel.id
      ? await run(() => updateHotel(hotel.id, hotel), 'Property details updated.')
      : await run(() => createHotel(hotel), 'Property created as a draft.')
    if (data === false) return
    setHotel(data)
    setPortfolio((current) => hotel.id
      ? current.map((item) => item.id === data.id ? data : item)
      : [data, ...current])
  }
  const addRoom = async (event) => {
    event.preventDefault()
    const data = await run(() => createRoom(hotel.id, {
      ...room,
      basePrice: Number(room.basePrice),
      capacity: Number(room.capacity),
      totalCount: Number(room.totalCount),
    }), 'Room type added.')
    if (data !== false) {
      setRooms((current) => [...current, data])
      setRoom(emptyRoom)
    }
  }
  const activate = async () => {
    const result = await run(() => activateHotel(hotel.id), 'Property activated with one year of inventory.')
    if (result !== false) {
      const activeHotel = { ...hotel, isActive: true }
      setHotel(activeHotel)
      setPortfolio((current) => current.map((item) => item.id === hotel.id ? activeHotel : item))
    }
  }
  const removeProperty = async () => {
    const id = hotel.id
    const result = await run(() => deleteHotel(id), 'Property deleted.')
    if (result !== false) {
      const remaining = portfolio.filter((item) => item.id !== id)
      setPortfolio(remaining)
      if (remaining.length > 0) openProperty(remaining[0])
      else startNew()
    }
  }

  const activeCount = portfolio.filter((item) => item.isActive).length
  const visibleBookings = useMemo(() => dashboard.bookings.filter((booking) => {
    const matchesProperty = bookingProperty === 'all' || String(booking.hotelId) === bookingProperty
    const matchesStatus = bookingStatus === 'all' || booking.bookingStatus === bookingStatus
    return matchesProperty && matchesStatus
  }), [dashboard.bookings, bookingProperty, bookingStatus])
  const formatDate = (date) => new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(`${date}T12:00:00`))
  const formatMoney = (amount) => new Intl.NumberFormat('en-DE', { style: 'currency', currency: 'EUR' }).format(Number(amount || 0))

  return (
    <main className="admin-page">
      <section className="admin-header">
        <div><span className="pill">Property workspace</span><h1>Your stays,<br />one dashboard.</h1><p>Manage only the properties assigned to your manager account.</p></div>
        <div className="admin-metrics"><span><strong>{formatMoney(dashboard.confirmedRevenue)}</strong> confirmed revenue</span><span><strong>{dashboard.confirmedBookings}</strong> confirmed stays</span><span><strong>{dashboard.arrivalsNextSevenDays}</strong> arrivals in 7 days</span><span><strong>{activeCount}/{portfolio.length}</strong> properties live</span></div>
      </section>
      <section className="admin-shell">
        <div className="property-toolbar"><div><span className="eyebrow">My portfolio</span><h2>Properties</h2></div><button className="primary-button" type="button" onClick={startNew}>+ Add property</button></div>
        {loading ? <div className="property-loading">Loading your properties…</div> : <div className="property-list" aria-label="Your properties">
          {portfolio.map((item) => <button className={`property-card ${hotel.id === item.id ? 'selected' : ''}`} type="button" key={item.id} onClick={() => openProperty(item)}><span className={`property-icon ${item.isActive ? 'active' : ''}`}>{item.name.charAt(0)}</span><span><strong>{item.name}</strong><small>{item.city} · {item.isActive ? 'Live' : 'Draft'}</small></span><i>→</i></button>)}
          {portfolio.length === 0 && <div className="property-empty">No properties yet. Create your first stay.</div>}
        </div>}
        {notice.text && <div className={`form-message ${notice.type}`}>{notice.text}</div>}
        <div className="admin-columns">
          <form className="admin-card" onSubmit={save}>
            <div className="admin-card-heading"><div><span className="eyebrow">Property details</span><h2>{hotel.id ? hotel.name : 'New property'}</h2></div>{hotel.id && <span className={`status-chip ${hotel.isActive ? 'active' : ''}`}>{hotel.isActive ? 'Active' : 'Draft'}</span>}</div>
            <label>Hotel name<input required name="name" value={hotel.name || ''} onChange={changeHotel} /></label>
            <label>City<input required name="city" value={hotel.city || ''} onChange={changeHotel} /></label>
            <label>Amenities<input name="amenities" value={hotel.amenities?.join(', ') || ''} onChange={changeHotel} placeholder="Wi-Fi, breakfast, parking" /></label>
            <button className="primary-button admin-submit">{hotel.id ? 'Save changes' : 'Create draft'}</button>
            {hotel.id && <div className="admin-actions"><button type="button" className="outline-button" disabled={hotel.isActive} onClick={activate}>{hotel.isActive ? 'Property live' : 'Activate'}</button><button type="button" className="danger-button" onClick={removeProperty}>Delete</button></div>}
          </form>
          <div className="admin-card">
            <div className="admin-card-heading"><div><span className="eyebrow">Inventory setup</span><h2>Rooms</h2></div><span>{rooms.length} types</span></div>
            {!hotel.id ? <div className="empty-state">Create or select a property before adding rooms.</div> : <><form className="room-form" onSubmit={addRoom}><div className="form-row"><label>Room type<input required name="type" value={room.type} onChange={changeRoom} placeholder="Family room" /></label><label>Price (€)<input required min="1" type="number" name="basePrice" value={room.basePrice} onChange={changeRoom} /></label></div><div className="form-row"><label>Capacity<input required min="1" type="number" name="capacity" value={room.capacity} onChange={changeRoom} /></label><label>Total rooms<input required min="1" type="number" name="totalCount" value={room.totalCount} onChange={changeRoom} /></label></div><label>Amenities<input name="amenities" value={room.amenities.join(', ')} onChange={changeRoom} /></label><button className="outline-button">Add room type</button></form><div className="admin-room-list">{rooms.map((item) => <div key={item.id}><span><strong>{item.type}</strong><small>{item.capacity} guests · {item.totalCount} rooms · €{item.basePrice}</small></span><button className="icon-danger" aria-label={`Delete ${item.type}`} onClick={async () => { const result = await run(() => deleteRoom(hotel.id, item.id), 'Room type deleted.'); if (result !== false) setRooms((current) => current.filter((existing) => existing.id !== item.id)) }}>×</button></div>)}</div></>}
          </div>
        </div>
        <section className="reservation-panel">
          <div className="reservation-heading"><div><span className="eyebrow">Front desk overview</span><h2>Reservations</h2><p>{visibleBookings.length} of {dashboard.totalBookings} bookings shown</p></div><div className="reservation-filters"><label>Property<select value={bookingProperty} onChange={(event) => setBookingProperty(event.target.value)}><option value="all">All properties</option>{portfolio.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Status<select value={bookingStatus} onChange={(event) => setBookingStatus(event.target.value)}><option value="all">All statuses</option><option value="CONFIRMED">Confirmed</option><option value="RESERVED">Reserved</option><option value="GUEST_ADDED">Guest added</option><option value="PAYMENT_PENDING">Payment pending</option><option value="CANCELLED">Cancelled</option><option value="EXPIRED">Expired</option></select></label></div></div>
          {visibleBookings.length === 0 ? <div className="reservation-empty"><span>▤</span><h3>No reservations match</h3><p>New bookings for your properties will appear here automatically.</p></div> : <div className="reservation-list">{visibleBookings.map((booking) => <article className="reservation-card" key={booking.id}><div className="reservation-guest"><span className="reservation-avatar">{booking.leadGuest.charAt(0)}</span><div><small>Booking #{booking.id}</small><strong>{booking.leadGuest}</strong><span>{booking.guestCount} guest{booking.guestCount === 1 ? '' : 's'} · {booking.roomsCount} room{booking.roomsCount === 1 ? '' : 's'}</span></div></div><div className="reservation-property"><small>Property & room</small><strong>{booking.hotelName}</strong><span>{booking.roomType}</span></div><div className="reservation-dates"><small>Stay</small><strong>{formatDate(booking.checkInDate)}</strong><span>to {formatDate(booking.checkOutDate)}</span></div><div className="reservation-value"><span className={`status-chip ${booking.bookingStatus === 'CONFIRMED' ? 'active' : ''}`}>{booking.bookingStatus.replaceAll('_', ' ')}</span><strong>{formatMoney(booking.amount)}</strong><small>{booking.paymentStatus ? `Payment ${booking.paymentStatus.toLowerCase()}` : 'Awaiting payment'}</small></div></article>)}</div>}
        </section>
      </section>
    </main>
  )
}
