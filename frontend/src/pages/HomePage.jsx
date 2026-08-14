import SearchBar from '../components/SearchBar'
import HotelCard from '../components/HotelCard'
import { demoHotels } from '../data/demoHotels'

const benefits = [
  ['01', 'A room for every trip', 'Social dorms, calm private rooms and practical family spaces.'],
  ['02', 'City-centre locations', 'Spend less time commuting and more time discovering the city.'],
  ['03', 'Friendly, flexible stays', 'Helpful teams, easy changes and support around the clock.'],
  ['04', 'Travel more responsibly', 'Thoughtful operations designed to reduce waste and energy use.'],
]

export default function HomePage() {
  return (
    <main>
      <section className="hero">
        <div className="hero-shape shape-one" />
        <div className="hero-shape shape-two" />
        <div className="hero-copy">
          <span className="pill">City breaks, made simple</span>
          <h1>Stay central.<br /><em>Feel at home.</em></h1>
          <p>Friendly hotels and hostels for spontaneous weekends, family adventures and everything in between.</p>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="sun" />
          <div className="building building-back"><span /><span /><span /><span /></div>
          <div className="building building-front"><span /><span /><span /><span /><span /><span /></div>
          <div className="traveller"><div className="head" /><div className="body" /><div className="bag" /></div>
        </div>
        <div className="hero-search"><SearchBar /></div>
      </section>

      <section className="trust-strip" aria-label="Booking benefits">
        <span>✓ Best price promise</span><span>✓ Free Wi-Fi</span><span>✓ 24/7 support</span><span>✓ Flexible options</span>
      </section>

      <section className="section" id="stays">
        <div className="section-heading"><div><span className="eyebrow">Popular right now</span><h2>Find your next city</h2></div><button className="outline-button" type="button">Explore all stays</button></div>
        <div className="hotel-grid">{demoHotels.map((hotel) => <HotelCard hotel={hotel} key={hotel.id} />)}</div>
      </section>

      <section className="benefits-section" id="why">
        <div className="benefit-intro"><span className="eyebrow">Why Stayly?</span><h2>More trip.<br />Less fuss.</h2><p>Good stays should be easy to find, fair to book and welcoming when you arrive.</p></div>
        <div className="benefit-grid">{benefits.map(([number, title, copy]) => <article key={number}><span>{number}</span><h3>{title}</h3><p>{copy}</p></article>)}</div>
      </section>

      <section className="club-banner" id="groups">
        <div><span className="pill light">Stayly Circle</span><h2>A little more stay<br />for a little less.</h2><p>Members save 15% and get first access to our best city deals.</p><button className="light-button" type="button">Join for free →</button></div>
        <div className="club-badge"><strong>15%</strong><span>member saving</span></div>
      </section>
    </main>
  )
}
