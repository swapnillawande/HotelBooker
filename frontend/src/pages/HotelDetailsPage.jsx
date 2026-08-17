import { useEffect, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { getHotelInfo } from '../api/hotels'
import { demoHotels } from '../data/demoHotels'

export default function HotelDetailsPage() {
  const { hotelId } = useParams()
  const [params] = useSearchParams()
  const fallback = demoHotels.find((hotel) => String(hotel.id) === String(hotelId)) || demoHotels[0]
  const [details, setDetails] = useState({ hotel: fallback, rooms: fallback.rooms })
  const [live, setLive] = useState(false)

  useEffect(() => {
    getHotelInfo(hotelId).then((data) => {
      setDetails({ hotel: { ...fallback, ...data.hotel }, rooms: data.rooms?.length ? data.rooms : fallback.rooms })
      setLive(true)
    }).catch(() => setLive(false))
  }, [hotelId])

  const { hotel, rooms } = details
  const bookingParams = new URLSearchParams({
    hotelId,
    checkIn: params.get('checkIn') || new Date(Date.now() + 86400000).toISOString().slice(0, 10),
    checkOut: params.get('checkOut') || new Date(Date.now() + 172800000).toISOString().slice(0, 10),
    rooms: params.get('rooms') || '1',
    guests: params.get('guests') || '2',
  })

  return (
    <main className="details-page">
      <section className="details-hero">
        <img src={hotel.image || fallback.image} alt={hotel.name} />
        <div className="details-overlay"><span className="pill">{live ? 'Live availability' : 'Preview availability'}</span><h1>{hotel.name}</h1><p>⌖ {hotel.city} · Central city location</p></div>
      </section>
      <section className="details-content">
        <div className="property-summary"><div><span className="eyebrow">Your city base</span><h2>Easy stays, good energy.</h2><p>{hotel.description || fallback.description}</p></div><div className="property-score"><strong>{hotel.rating || fallback.rating}</strong><span>Excellent stay</span></div></div>
        <div className="feature-row"><span>✓ Free Wi-Fi</span><span>✓ 24-hour reception</span><span>✓ Central location</span><span>✓ Flexible rooms</span></div>
        <div className="room-heading"><div><span className="eyebrow">Choose your room</span><h2>Sleep your way</h2></div><p>{bookingParams.get('checkIn')} — {bookingParams.get('checkOut')}</p></div>
        <div className="room-list">{rooms.map((room, index) => {
          const paramsWithRoom = new URLSearchParams(bookingParams)
          paramsWithRoom.set('roomId', room.id)
          const requiredRooms = Math.ceil(Number(bookingParams.get('guests')) / room.capacity)
          paramsWithRoom.set('rooms', String(Math.max(Number(bookingParams.get('rooms')), requiredRooms)))
          return <article className="room-card" key={room.id}>
            <div className={`room-visual room-visual-${index % 3}`}><span>{room.capacity} guests</span></div>
            <div className="room-copy"><span className="eyebrow">{room.totalCount} rooms available</span><h3>{room.type}</h3><div className="amenity-list">{room.amenities?.map((item) => <span key={item}>✓ {item}</span>)}</div><p>Comfortable, practical and ready for your city adventure.</p></div>
            <div className="room-price"><small>per room / night</small><strong>€{room.basePrice}</strong><Link className="primary-button" to={`/booking?${paramsWithRoom}`}>Select room →</Link></div>
          </article>
        })}</div>
      </section>
    </main>
  )
}
