import { useEffect, useState } from 'react'
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

const emptyHotel = { name: '', city: '', amenities: [], photos: [], contactInfo: {} }
const emptyRoom = { type: '', basePrice: '', totalCount: '', capacity: '', amenities: [], photos: [] }

export default function AdminPage() {
  const [portfolio, setPortfolio] = useState([])
  const [hotel, setHotel] = useState(emptyHotel)
  const [rooms, setRooms] = useState([])
  const [room, setRoom] = useState(emptyRoom)
  const [loading, setLoading] = useState(true)
  const [notice, setNotice] = useState({ type: '', text: '' })

  useEffect(() => {
    let cancelled = false
    getAdminHotels()
      .then(async (properties) => {
        if (cancelled) return
        setPortfolio(properties)
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

  return (
    <main className="admin-page">
      <section className="admin-header">
        <div><span className="pill">Property workspace</span><h1>Your stays,<br />one dashboard.</h1><p>Manage only the properties assigned to your manager account.</p></div>
        <div className="admin-metrics"><span><strong>{portfolio.length}</strong> properties</span><span><strong>{activeCount}</strong> live</span><span><strong>{rooms.length}</strong> room types here</span></div>
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
      </section>
    </main>
  )
}
