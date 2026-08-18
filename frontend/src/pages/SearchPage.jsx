import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import SearchBar from '../components/SearchBar'
import HotelCard from '../components/HotelCard'
import { searchHotels } from '../api/hotels'

export default function SearchPage() {
  const [params] = useSearchParams()
  const criteria = Object.fromEntries(params)
  const [hotels, setHotels] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [maxPrice, setMaxPrice] = useState(150)
  const [facilities, setFacilities] = useState([])
  const [sort, setSort] = useState('recommended')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    searchHotels(criteria)
      .then((results) => {
        if (!cancelled) setHotels(results)
      })
      .catch((requestError) => {
        if (!cancelled) {
          setHotels([])
          setError(requestError.message || 'We could not load stays. Please try again.')
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => { cancelled = true }
  }, [params.toString()])

  const visibleHotels = useMemo(() => {
    const filtered = hotels.filter((hotel) => {
      const price = Number(hotel.startingPrice ?? hotel.price ?? Number.POSITIVE_INFINITY)
      const amenities = (hotel.amenities || []).map((item) => item.toLowerCase())
      return price <= maxPrice && facilities.every((facility) => amenities.some((item) => item.includes(facility)))
    })
    return [...filtered].sort((first, second) => {
      const firstPrice = Number(first.startingPrice ?? first.price ?? 0)
      const secondPrice = Number(second.startingPrice ?? second.price ?? 0)
      if (sort === 'price-low') return firstPrice - secondPrice
      if (sort === 'price-high') return secondPrice - firstPrice
      if (sort === 'rating') return Number(second.rating ?? 0) - Number(first.rating ?? 0)
      return 0
    })
  }, [hotels, maxPrice, facilities, sort])

  const toggleFacility = (facility) => setFacilities((current) => current.includes(facility)
    ? current.filter((item) => item !== facility)
    : [...current, facility])

  return (
    <main className="results-page">
      <div className="search-page-bar"><SearchBar compact initial={criteria} /></div>
      <section className="results-layout">
        <aside className="filters">
          <span className="eyebrow">Narrow it down</span><h2>Filters</h2>
          <label>Maximum price: €{maxPrice}<input aria-label="Maximum nightly price" type="range" min="10" max="150" value={maxPrice} onChange={(event) => setMaxPrice(Number(event.target.value))} /><span className="range-label"><small>€10</small><small>€150</small></span></label>
          <fieldset><legend>Facilities</legend>{[['wi-fi', 'Free Wi-Fi'], ['breakfast', 'Breakfast'], ['kitchen', 'Guest kitchen']].map(([value, label]) => <label key={value}><input type="checkbox" checked={facilities.includes(value)} onChange={() => toggleFacility(value)} /> {label}</label>)}</fieldset>
          {(facilities.length > 0 || maxPrice < 150) && <button className="filter-reset" type="button" onClick={() => { setFacilities([]); setMaxPrice(150) }}>Clear filters</button>}
        </aside>
        <div className="results-main">
          <div className="results-heading"><div><span className="eyebrow">{criteria.checkIn} — {criteria.checkOut}</span><h1>Stays in {criteria.city || 'Europe'}</h1><p>{loading ? 'Finding your best options…' : error ? 'We could not load stays' : `${visibleHotels.length} of ${hotels.length} welcoming stays match`}</p></div><select aria-label="Sort results" value={sort} onChange={(event) => setSort(event.target.value)}><option value="recommended">Our top picks</option><option value="price-low">Lowest price</option><option value="price-high">Highest price</option><option value="rating">Highest rating</option></select></div>
          <div className="results-list">
            {loading && [1,2,3].map((item) => <div className="hotel-skeleton" key={item} />)}
            {!loading && error && <div className="form-message error" role="alert">{error}</div>}
            {!loading && !error && visibleHotels.length === 0 && <div className="empty-state">No stays match these dates and filters. Try widening your search.</div>}
            {!loading && !error && visibleHotels.map((hotel) => <HotelCard hotel={hotel} horizontal key={hotel.id} />)}
          </div>
        </div>
      </section>
    </main>
  )
}
