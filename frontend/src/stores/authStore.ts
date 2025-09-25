import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { UserInfo } from '../types/common';

interface AuthState {
  user: UserInfo | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (user: UserInfo, token: string) => void;
  logout: () => void;
  updateUser: (user: UserInfo) => void;
}

const useAuthStore = create<AuthState>()(
  persist(
    (set: any) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (user: UserInfo, token: string) => set({ user, token, isAuthenticated: true }),
      logout: () => set({ user: null, token: null, isAuthenticated: false }),
      updateUser: (user: UserInfo) => set({ user }),
    }),
    {
      name: 'auth-storage',
    }
  )
);

export default useAuthStore;