import { useEffect, useState } from 'react'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/services/api/auth'

export const useAuthInit = () => {
  const [isInitialized, setIsInitialized] = useState(false)
  const { isAuthenticated, login, logout, token } = useAuthStore()

  useEffect(() => {
    const initAuth = async () => {
      // 检查localStorage中是否有token
      const storedToken = localStorage.getItem('token')
      
      // 如果zustand状态中没有认证但localStorage中有token，需要验证token有效性
      if (!isAuthenticated && storedToken) {
        try {
          // 调用后端接口验证token有效性
          const response = await authApi.getCurrentUser()
          if (response.data) {
            // token有效，更新认证状态
            login(response.data, storedToken)
          } else {
            // token无效，清理状态
            logout()
          }
        } catch (error) {
          // token验证失败，清理状态
          logout()
        }
      }
      
      setIsInitialized(true)
    }

    initAuth()
  }, [isAuthenticated, login, logout])

  return { isInitialized }
}