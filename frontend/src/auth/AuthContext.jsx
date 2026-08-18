import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { getCurrentUser, loginAccount, logoutAccount, registerAccount } from '../api/auth'
import { clearAuthSession, getAuthSession, saveAuthSession } from './session'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [session, setSession] = useState(getAuthSession)
  const [initializing, setInitializing] = useState(Boolean(getAuthSession()?.accessToken))

  useEffect(() => {
    if (!session?.accessToken) {
      setInitializing(false)
      return
    }

    let active = true
    getCurrentUser()
      .then((user) => {
        if (!active) return
        const refreshed = { ...session, user }
        saveAuthSession(refreshed)
        setSession(refreshed)
      })
      .catch(() => {
        if (!active) return
        clearAuthSession()
        setSession(null)
      })
      .finally(() => active && setInitializing(false))

    return () => { active = false }
    // A saved token only needs validation when the provider mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const storeSession = (nextSession) => {
    saveAuthSession(nextSession)
    setSession(nextSession)
    return nextSession.user
  }

  const login = async (credentials) => storeSession(await loginAccount(credentials))
  const register = async (details) => storeSession(await registerAccount(details))
  const logout = async () => {
    try {
      await logoutAccount()
    } finally {
      clearAuthSession()
      setSession(null)
    }
  }

  const value = useMemo(() => ({
    user: session?.user || null,
    initializing,
    login,
    register,
    logout,
  }), [session, initializing])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
