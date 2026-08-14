export default function HotelCard({ hotel, horizontal = false }) {
  return (
    <article className={`hotel-card ${horizontal ? 'horizontal' : ''}`}>
      <div className="hotel-image-wrap">
        <img className="hotel-image" src={hotel.image} alt={`${hotel.name} accommodation`} />
        <span className="card-tag">{hotel.tag}</span>
        <button className="heart-button" aria-label={`Save ${hotel.name}`} type="button">♡</button>
      </div>
      <div className="hotel-content">
        <div className="hotel-heading">
          <div><span className="eyebrow">{hotel.city}</span><h3>{hotel.name}</h3></div>
          <div className="rating"><strong>{hotel.rating}</strong><span>{hotel.reviews} reviews</span></div>
        </div>
        {hotel.description && <p>{hotel.description}</p>}
        <div className="amenity-list">{hotel.amenities?.map((amenity) => <span key={amenity}>✓ {amenity}</span>)}</div>
        <div className="price-row"><span>From <strong>€{hotel.price}</strong> / night</span><button type="button" className="text-button">View rooms →</button></div>
      </div>
    </article>
  )
}
