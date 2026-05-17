import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  token: localStorage.getItem('jwt_token'),
  refreshToken: localStorage.getItem('jwt_refresh_token'),
  isAuthenticated: !!localStorage.getItem('jwt_token'),

  setAuth: (user, token, refreshToken) => {
    localStorage.setItem('user', JSON.stringify(user))
    localStorage.setItem('jwt_token', token)
    if (refreshToken) {
      localStorage.setItem('jwt_refresh_token', refreshToken)
    }
    set({ 
      user, 
      token, 
      refreshToken: refreshToken || localStorage.getItem('jwt_refresh_token'), 
      isAuthenticated: true 
    })
  },

  logout: () => {
    localStorage.removeItem('user')
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('jwt_refresh_token')
    set({ user: null, token: null, refreshToken: null, isAuthenticated: false })
  }
}))

