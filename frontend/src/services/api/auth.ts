import http from '../http/client'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
}

export interface MeResponse {
  id: number
  username: string
  email: string
  fullName?: string
}

type ApiResponse<T> = { code: number; message: string; data: T }

export const authApi = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const resp = await http.post<ApiResponse<AuthResponse>>('/auth/login', payload)
    return resp.data.data
  },

  async register(payload: RegisterRequest): Promise<void> {
    await http.post<ApiResponse<AuthResponse>>('/auth/register', payload)
  },

  async me(): Promise<MeResponse> {
    const resp = await http.get<ApiResponse<MeResponse>>('/auth/me')
    return resp.data.data
  },
}


