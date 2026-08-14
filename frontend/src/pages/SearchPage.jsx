import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import SearchBar from '../components/SearchBar'
import HotelCard from '../components/HotelCard'
import { searchHotels } from '../api/hotels'

export default function SearchPage() {
  const [params] = useSearchParams()
  const criteria = Object.fromEntries(params)
  const [hotels, setHotels] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    searchHotels(criteria).then(setHotels).finally(() => setLoading(false))
  }, [params.toString()])

  return (
    <main className="results-page">
      <div className="search-page-bar"><SearchBar compact initial={criteria} /></div>
      <section className="results-layout">
        <aside className="filters">
          <span className="eyebrow">Narrow it down</span><h2>Filters</h2>
          <label>Price per night<input type="range" min="10" max="150" defaultValue="90" /><span className="range-label"><small>€10</small><small>€150+</small></span></label>
          <fieldset><legend>Room type</legend>{['Private room', 'Family room', 'Shared dorm'].map((item) => <label key={item}><input type="checkbox" /> {item}</label>)}</fieldset>
          <fieldset><legend>Facilities</legend>{['Breakfast', 'Guest kitchen', 'Parking'].map((item) => <label key={item}><input type="checkbox" /> {item}</label>)}</fieldset>
        </aside>
        <div className="results-main">
          <div className="results-heading"><div><span className="eyebrow">{criteria.checkIn} — {criteria.checkOut}</span><h1>Stays in {criteria.city || 'Europe'}</h1><p>{loading ? 'Finding your best options…' : `${hotels.length} welcoming stays ready for you`}</p></div><select aria-label="Sort results"><option>Our top picks</option><option>Lowest price</option><option>Highest rating</option></select></div>
          <div className="results-list">{loading ? [1,2,3].map((item) => <div className="hotel-skeleton" key={item} />) : hotels.map((hotel) => <HotelCard hotel={hotel} horizontal key={hotel.id} />)}</div>
        </div>
      </section>
    </main>
  )
}
