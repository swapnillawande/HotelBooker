import { Route, Routes } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import SearchPage from './pages/SearchPage'

export default function App() {
  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/search" element={<SearchPage />} />
      </Routes>
      <footer><div className="brand footer-brand"><span className="brand-mark">S</span><span>stayly</span></div><p>Friendly city stays for everyone.</p><small>© 2026 Stayly. Built for better city breaks.</small></footer>
    </>
  )
}
