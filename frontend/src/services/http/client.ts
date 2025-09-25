import axios from 'axios'

function resolveApiBase(): string {
  const envUrl = import.meta.env.VITE_API_BASE_URL as string | undefined
  if (envUrl) return envUrl
  // Fallbacks to avoid localhost DNS issues
  const protocol = typeof window !== 'undefined' ? window.location.protocol : 'http:'
  const host = typeof window !== 'undefined' ? window.location.hostname : '127.0.0.1'
  const baseHost = host || '127.0.0.1'
  return `${protocol}//${baseHost}:8080/api`
}

const apiBaseUrl = resolveApiBase()

export const httpClient = axios.create({
  baseURL: apiBaseUrl,
  timeout: 15000,
})

httpClient.interceptors.request.use((config) => {
  try {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
  } catch (_) {
    // ignore storage errors
  }
  return config
})

httpClient.interceptors.response.use(
  (resp) => resp,
  (error) => {
    if (error?.response?.status === 401) {
      try {
        localStorage.removeItem('token')
      } catch (_) {}
      // best-effort redirect to login if not already there
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default httpClient


