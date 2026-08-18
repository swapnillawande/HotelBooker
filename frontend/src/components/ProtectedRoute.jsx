import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function ProtectedRoute({ children, role }) {
  const { user, initializing } = useAuth()
  const location = useLocation()

  if (initializing) return <main className="account-page"><div className="account-loading">Loading your account…</div></main>
  if (!user) {
    const returnTo = `${location.pathname}${location.search}`
    return <Navigate to={`/account?returnTo=${encodeURIComponent(returnTo)}`} replace />
  }
  if (role && !user.roles?.includes(role)) return <Navigate to="/my-bookings" replace />
  return children
}
