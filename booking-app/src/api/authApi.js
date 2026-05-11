import apiClient from './apiClient'

export const authApi = {
  login: async (email, password) => {
    const response = await apiClient.post('/api/auth/login', { email, password })
    return response.data
  },

  register: async (userData) => {
    const response = await apiClient.post('/api/auth/register', userData)
    return response.data
  },

  logout: () => {
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('user')
  }
}

