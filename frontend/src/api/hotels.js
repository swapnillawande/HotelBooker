import { demoHotels } from '../data/demoHotels'
import { apiRequest } from './client'

export async function searchHotels(criteria) {
  const getDemoResults = () => {
    const city = criteria.city.trim().toLowerCase()
    const matching = demoHotels.filter((hotel) => hotel.city.toLowerCase().includes(city))
    return matching.length ? matching : demoHotels
  }

  try {
    const page = await apiRequest('/hotels/search', {
      method: 'POST',
      body: JSON.stringify({
        city: criteria.city,
        startDate: criteria.checkIn,
        endDate: criteria.checkOut,
        roomsCount: criteria.rooms,
        page: 0,
        size: 12,
      }),
    })

    const hotels = (page.content ?? []).map((hotel, index) => ({
      ...demoHotels[index % demoHotels.length],
      ...hotel,
    }))
    return hotels.length ? hotels : getDemoResults()
  } catch {
    return getDemoResults()
  }
}

export async function getHotelInfo(hotelId) {
  return apiRequest(`/hotels/${hotelId}/info`)
}

export async function createHotel(hotel) {
  return apiRequest('/admin/hotels', { method: 'POST', body: JSON.stringify(hotel) })
}

export async function getAdminHotel(hotelId) {
  return apiRequest(`/admin/hotels/${hotelId}`)
}

export async function updateHotel(hotelId, hotel) {
  return apiRequest(`/admin/hotels/${hotelId}`, { method: 'PUT', body: JSON.stringify(hotel) })
}

export async function activateHotel(hotelId) {
  return apiRequest(`/admin/hotels/${hotelId}/activate`, { method: 'PATCH' })
}

export async function deleteHotel(hotelId) {
  return apiRequest(`/admin/hotels/${hotelId}`, { method: 'DELETE' })
}

export async function getHotelRooms(hotelId) {
  return apiRequest(`/admin/hotels/${hotelId}/rooms`)
}

export async function createRoom(hotelId, room) {
  return apiRequest(`/admin/hotels/${hotelId}/rooms`, { method: 'POST', body: JSON.stringify(room) })
}

export async function deleteRoom(hotelId, roomId) {
  return apiRequest(`/admin/hotels/${hotelId}/rooms/${roomId}`, { method: 'DELETE' })
}
