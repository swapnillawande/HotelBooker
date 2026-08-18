import { useState } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function AccountPage() {
  const { user, initializing, login, register } = useAuth()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [status, setStatus] = useState('idle')
  const [message, setMessage] = useState('')
  const requestedPath = params.get('returnTo')
  const destination = requestedPath?.startsWith('/') ? requestedPath : '/my-bookings'

  if (!initializing && user) return <Navigate to={destination} replace />

  const update = (field, value) => setForm((current) => ({ ...current, [field]: value }))
  const submit = async (event) => {
    event.preventDefault()
    setStatus('loading')
    setMessage('')
    try {
      if (mode === 'login') {
        await login({ email: form.email, password: form.password })
      } else {
        await register({ name: form.name, email: form.email, password: form.password })
      }
      navigate(destination, { replace: true })
    } catch (error) {
      setMessage(error.message)
      setStatus('error')
    }
  }

  return (
    <main className="account-page">
      <section className="account-promo">
        <span className="pill light">Stayly account</span>
        <h1>Every stay,<br />in one place.</h1>
        <p>Book faster, review upcoming trips, and cancel eligible reservations without searching for an access code.</p>
        <div className="account-benefits"><span>✓ Secure booking history</span><span>✓ One-click cancellations</span><span>✓ Guest booking access still available</span></div>
      </section>
      <section className="account-panel">
        <div className="account-tabs" role="tablist" aria-label="Account action">
          <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => { setMode('login'); setMessage('') }}>Sign in</button>
          <button type="button" className={mode === 'register' ? 'active' : ''} onClick={() => { setMode('register'); setMessage('') }}>Create account</button>
        </div>
        <form className="account-form" onSubmit={submit}>
          <span className="eyebrow">{mode === 'login' ? 'Welcome back' : 'Join Stayly'}</span>
          <h2>{mode === 'login' ? 'Sign in to your trips.' : 'Create your account.'}</h2>
          {mode === 'register' && <label>Full name<input required maxLength="100" autoComplete="name" value={form.name} onChange={(event) => update('name', event.target.value)} placeholder="Alex Morgan" /></label>}
          <label>Email address<input required type="email" autoComplete="email" value={form.email} onChange={(event) => update('email', event.target.value)} placeholder="alex@example.com" /></label>
          <label>Password<input required minLength="8" maxLength="72" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} value={form.password} onChange={(event) => update('password', event.target.value)} placeholder="At least 8 characters" /></label>
          {status === 'error' && <div className="form-message error" role="alert">{message}</div>}
          <button className="primary-button account-submit" disabled={status === 'loading'}>{status === 'loading' ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>
        <div className="demo-login"><strong>Try the demo</strong><span>demo@stayly.local</span><span>Password: StaylyDemo123!</span></div>
        <p className="guest-access-link">Booked without an account? <Link to="/manage-booking">Use your booking code</Link></p>
      </section>
    </main>
  )
}
