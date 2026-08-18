import { Route, Routes } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import SearchPage from './pages/SearchPage'
import HotelDetailsPage from './pages/HotelDetailsPage'
import BookingPage from './pages/BookingPage'
import AdminPage from './pages/AdminPage'
import ManageBookingPage from './pages/ManageBookingPage'
import AccountPage from './pages/AccountPage'
import MyBookingsPage from './pages/MyBookingsPage'
import ProtectedRoute from './components/ProtectedRoute'

export default function App() {
  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/hotels/:hotelId" element={<HotelDetailsPage />} />
        <Route path="/booking" element={<BookingPage />} />
        <Route path="/manage-booking" element={<ManageBookingPage />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/my-bookings" element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>} />
        <Route path="/admin" element={<ProtectedRoute role="HOTEL_MANAGER"><AdminPage /></ProtectedRoute>} />
      </Routes>
      <footer><div className="brand footer-brand"><span className="brand-mark">S</span><span>stayly</span></div><p>Friendly city stays for everyone.</p><small>© 2026 Stayly. Built for better city breaks.</small></footer>
    </>
  )
}
