import { Link } from 'react-router-dom'

export default function Header() {
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
        <Link to="/admin">Property admin</Link>
      </nav>
      <div className="header-actions">
        <button className="language-button" type="button" aria-label="Change language">EN · €</button>
        <Link className="outline-button" to="/manage-booking">Manage booking</Link>
      </div>
    </header>
  )
}
