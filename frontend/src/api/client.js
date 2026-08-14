const API_BASE = '/api/v1'

export class ApiRequestError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
  }
}

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  })

  const payload = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    const message = payload?.error?.message || payload?.message || `Request failed (${response.status})`
    throw new ApiRequestError(message, response.status)
  }

  return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload
}
