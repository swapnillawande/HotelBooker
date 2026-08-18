import { useEffect, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { getHotelInfo, getRoomOffers } from '../api/hotels'
import { demoHotels } from '../data/demoHotels'

export default function HotelDetailsPage() {
  const { hotelId } = useParams()
  const [params] = useSearchParams()
  const fallback = demoHotels.find((hotel) => String(hotel.id) === String(hotelId)) || demoHotels[0]
  const [details, setDetails] = useState({ hotel: fallback, rooms: [] })
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')

  const criteria = {
    checkIn: params.get('checkIn') || new Date(Date.now() + 86400000).toISOString().slice(0, 10),
    checkOut: params.get('checkOut') || new Date(Date.now() + 172800000).toISOString().slice(0, 10),
    rooms: Number(params.get('rooms') || 1),
    guests: Number(params.get('guests') || 2),
  }

  useEffect(() => {
    let cancelled = false
    setStatus('loading')
    setError('')
    Promise.all([getHotelInfo(hotelId), getRoomOffers(hotelId, criteria)]).then(([data, offers]) => {
      if (!cancelled) {
        setDetails({ hotel: { ...fallback, ...data.hotel }, rooms: offers })
        setStatus('success')
      }
    }).catch((requestError) => {
      if (!cancelled) {
        setDetails((current) => ({ ...current, rooms: [] }))
        setError(requestError.message || 'Live room availability could not be loaded.')
        setStatus('error')
      }
    })
    return () => { cancelled = true }
  }, [hotelId, criteria.checkIn, criteria.checkOut, criteria.rooms])

  const { hotel, rooms } = details
  const selectableRooms = rooms.filter((room) => {
    const requiredRooms = Math.max(criteria.rooms, Math.ceil(criteria.guests / room.capacity))
    return room.availableRooms >= requiredRooms
  })
  const bookingParams = new URLSearchParams({
    hotelId,
    checkIn: criteria.checkIn,
    checkOut: criteria.checkOut,
    rooms: String(criteria.rooms),
    guests: String(criteria.guests),
  })

  return (
    <main className="details-page">
      <section className="details-hero">
        <img src={hotel.image || fallback.image} alt={hotel.name} />
        <div className="details-overlay"><span className="pill">{status === 'success' ? 'Live availability' : 'Checking availability'}</span><h1>{hotel.name}</h1><p>⌖ {hotel.city} · Central city location</p></div>
      </section>
      <section className="details-content">
        <div className="property-summary"><div><span className="eyebrow">Your city base</span><h2>Easy stays, good energy.</h2><p>{hotel.description || fallback.description}</p></div><div className="property-score"><strong>{hotel.rating || fallback.rating}</strong><span>Excellent stay</span></div></div>
        <div className="feature-row"><span>✓ Free Wi-Fi</span><span>✓ 24-hour reception</span><span>✓ Central location</span><span>✓ Flexible rooms</span></div>
        <div className="room-heading"><div><span className="eyebrow">Choose your room</span><h2>Live offers for your stay</h2></div><p>{bookingParams.get('checkIn')} — {bookingParams.get('checkOut')} · {criteria.rooms} room{criteria.rooms > 1 ? 's' : ''}</p></div>
        {status === 'loading' && <div className="room-list">{[1, 2].map((item) => <div className="room-skeleton" key={item} />)}</div>}
        {status === 'error' && <div className="availability-message error" role="alert"><strong>Availability unavailable</strong><span>{error}</span><Link className="outline-button" to={`/search?${bookingParams}`}>Back to search</Link></div>}
        {status === 'success' && selectableRooms.length === 0 && <div className="availability-message"><strong>No room fits this group</strong><span>Try another date range, fewer guests, or fewer rooms.</span><Link className="outline-button" to={`/search?${bookingParams}`}>Change search</Link></div>}
        {status === 'success' && <div className="room-list">{selectableRooms.map((room, index) => {
          const paramsWithRoom = new URLSearchParams(bookingParams)
          paramsWithRoom.set('roomId', room.id)
          const requiredRooms = Math.ceil(Number(bookingParams.get('guests')) / room.capacity)
          const selectedRooms = Math.max(Number(bookingParams.get('rooms')), requiredRooms)
          const selectedTotal = (Number(room.totalPrice) / criteria.rooms * selectedRooms).toFixed(2)
          paramsWithRoom.set('rooms', String(selectedRooms))
          paramsWithRoom.set('roomType', room.type)
          paramsWithRoom.set('nightlyPrice', room.nightlyPrice)
          paramsWithRoom.set('totalPrice', selectedTotal)
          paramsWithRoom.set('hotelName', hotel.name)
          return <article className="room-card" key={room.id}>
            <div className={`room-visual room-visual-${index % 3}`}><span>{room.capacity} guests</span></div>
            <div className="room-copy"><span className="eyebrow">{room.availableRooms} left for your stay</span><h3>{room.type}</h3><div className="amenity-list">{room.amenities?.map((item) => <span key={item}>✓ {item}</span>)}</div><p>Exact live price for {room.nights} night{room.nights > 1 ? 's' : ''}; taxes included.</p></div>
            <div className="room-price"><small>€{room.nightlyPrice} avg. / room</small><strong>€{selectedTotal}</strong><small className="stay-total-label">stay total · {selectedRooms} room{selectedRooms > 1 ? 's' : ''}</small><Link className="primary-button" to={`/booking?${paramsWithRoom}`}>Select room →</Link></div>
          </article>
        })}</div>}
      </section>
    </main>
  )
}
