import { create } from 'zustand'
import { authApi, type MeResponse } from '../services/api/auth'

interface AuthState {
  token: string | null
  user: MeResponse | null
  loading: boolean
  error: string | null
  setToken: (token: string | null) => void
  fetchMe: () => Promise<void>
  logout: () => void
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: (() => {
    try {
      return localStorage.getItem('token')
    } catch (_) {
      return null
    }
  })(),
  user: null,
  loading: false,
  error: null,

  setToken: (token) => {
    try {
      if (token) localStorage.setItem('token', token)
      else localStorage.removeItem('token')
    } catch (_) {}
    set({ token })
  },

  fetchMe: async () => {
    const { token } = get()
    if (!token) return
    set({ loading: true, error: null })
    try {
      const me = await authApi.me()
      set({ user: me, loading: false })
    } catch (e: any) {
      set({ error: e?.message || 'Failed to load user', loading: false })
    }
  },

  logout: () => {
    try {
      localStorage.removeItem('token')
    } catch (_) {}
    set({ token: null, user: null })
  },
}))


