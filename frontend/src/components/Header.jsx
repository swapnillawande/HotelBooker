import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Header() {
  const { user, logout } = useAuth()
  const isManager = user?.roles?.includes('HOTEL_MANAGER')

  return (
    <header className="site-header">
      <Link to="/" className="brand" aria-label="Stayly home">
        <span className="brand-mark">S</span>
        <span>stayly</span>
      </Link>
      <nav className="main-nav" aria-label="Main navigation">
        <a href="#stays">Stays</a>
        <a href="#why">Why Stayly</a>
        <a href="#groups">Groups</a>
        {isManager && <Link to="/admin">Property admin</Link>}
      </nav>
      <div className="header-actions">
        <button className="language-button" type="button" aria-label="Change language">EN · €</button>
        <Link className="outline-button" to="/manage-booking">Manage booking</Link>
        {user ? <div className="account-menu"><Link to="/my-bookings" className="account-link"><span className="account-avatar">{user.name?.charAt(0).toUpperCase() || 'S'}</span><span>{user.name}</span></Link><button type="button" onClick={logout}>Sign out</button></div> : <Link className="account-button" to="/account">Sign in</Link>}
      </div>
    </header>
  )
}
