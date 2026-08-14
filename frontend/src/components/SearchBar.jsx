import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10)
const dayAfter = new Date(Date.now() + 172800000).toISOString().slice(0, 10)

export default function SearchBar({ compact = false, initial = {} }) {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    city: initial.city || 'Berlin',
    checkIn: initial.checkIn || tomorrow,
    checkOut: initial.checkOut || dayAfter,
    guests: Number(initial.guests) || 2,
    rooms: Number(initial.rooms) || 1,
  })

  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value })
  const submit = (event) => {
    event.preventDefault()
    navigate(`/search?${new URLSearchParams(form).toString()}`)
  }

  return (
    <form className={`search-bar ${compact ? 'compact' : ''}`} onSubmit={submit}>
      <label className="search-field destination-field">
        <span>Where do you want to go?</span>
        <div className="field-control"><i aria-hidden="true">⌖</i><input name="city" value={form.city} onChange={update} placeholder="City or property" required /></div>
      </label>
      <label className="search-field">
        <span>Check in</span>
        <div className="field-control"><i aria-hidden="true">◇</i><input type="date" name="checkIn" value={form.checkIn} min={tomorrow} onChange={update} required /></div>
      </label>
      <label className="search-field">
        <span>Check out</span>
        <div className="field-control"><i aria-hidden="true">◇</i><input type="date" name="checkOut" value={form.checkOut} min={form.checkIn} onChange={update} required /></div>
      </label>
      <label className="search-field guest-field">
        <span>Guests & rooms</span>
        <div className="field-control"><i aria-hidden="true">○</i><select name="guests" value={form.guests} onChange={update} aria-label="Guests">
          {[1, 2, 3, 4, 5, 6].map((value) => <option key={value} value={value}>{value} guest{value > 1 ? 's' : ''}</option>)}
        </select></div>
      </label>
      <button className="primary-button search-button" type="submit">Find a stay <span>→</span></button>
    </form>
  )
}
