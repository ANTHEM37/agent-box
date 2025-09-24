import { httpClient } from '../http/client'
import type { User } from '@/stores/auth'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName?: string
}

export interface AuthResponse {
  token: string
  type: string
  user: User
}

export const authApi = {
  login: (data: LoginRequest) => 
    httpClient.post<AuthResponse>('/auth/login', data),
    
  register: (data: RegisterRequest) => 
    httpClient.post<AuthResponse>('/auth/register', data),
    
  getCurrentUser: () => 
    httpClient.get<User>('/auth/me'),
}