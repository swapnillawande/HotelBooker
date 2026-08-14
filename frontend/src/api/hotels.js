import { demoHotels } from '../data/demoHotels'

export async function searchHotels(criteria) {
  const getDemoResults = () => {
    const city = criteria.city.trim().toLowerCase()
    const matching = demoHotels.filter((hotel) => hotel.city.toLowerCase().includes(city))
    return matching.length ? matching : demoHotels
  }

  try {
    const response = await fetch('/api/v1/hotels/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        city: criteria.city,
        startDate: criteria.checkIn,
        endDate: criteria.checkOut,
        roomsCount: criteria.rooms,
        page: 0,
        size: 12,
      }),
    })

    if (!response.ok) throw new Error(`Search failed with status ${response.status}`)
    const page = await response.json()
    const hotels = (page.content ?? []).map((hotel, index) => ({
      ...demoHotels[index % demoHotels.length],
      ...hotel,
    }))
    return hotels.length ? hotels : getDemoResults()
  } catch {
    return getDemoResults()
  }
}
