import { create } from 'zustand';


export const useAuthStore = create((set) => ({
  token: '',
  userId: '',
  setToken: (token) => set({ token }),
  setUserId: (userId) => set({ userId }),
  setAuth: (userId, token) => set({ userId, token }),
  clear: () => set({ token: '', userId: '' }),
}));
