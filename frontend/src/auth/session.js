const STORAGE_KEY = 'stayly-auth-session'

export function getAuthSession() {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY)) || null
  } catch {
    return null
  }
}

export function getAccessToken() {
  return getAuthSession()?.accessToken || null
}

export function saveAuthSession(session) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearAuthSession() {
  sessionStorage.removeItem(STORAGE_KEY)
}
