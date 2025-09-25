import apiClient from '../http/client';
import { ApiResponse, AuthResponse } from '../../types/common';
import { LoginRequest, RegisterRequest } from '../../types/auth';

// 用户登录
export const login = async (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data);
  return response.data;
};

// 用户注册
export const register = async (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
  return response.data;
};

// 获取当前用户信息
export const getCurrentUser = async (): Promise<ApiResponse<AuthResponse>> => {
  const response = await apiClient.get<ApiResponse<AuthResponse>>('/auth/me');
  return response.data;
};