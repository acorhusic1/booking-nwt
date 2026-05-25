import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// BUG-005: globalni flag da paralelni 401-ovi ne triggeruju vise redirect-a
let isRedirecting = false

const forceLogout = () => {
  if (isRedirecting) return
  isRedirecting = true
  try {
    useAuthStore.getState().logout()
  } catch {
    localStorage.removeItem('user')
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('jwt_refresh_token')
  }
  // replace umjesto href — Back ne smije vratiti korisnika na zasticenu rutu
  window.location.replace('/login')
}

// Request interceptor - dodaj JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Silent Refresh Queue vars
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// Response interceptor - obradi greške i uradi automatski refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Presretni 401 Unauthorized i osiguraj da ne ulazimo u beskonačnu petlju
    if (error.response?.status === 401 && !originalRequest._retry) {

      // Ako se token već osvježava, stavi ovaj zahtjev u red čekanja
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return apiClient(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      const refreshToken = localStorage.getItem('jwt_refresh_token')

      if (refreshToken) {
        isRefreshing = true
        try {
          // Koristi običan axios instancu za refresh da izbjegneš presretanje requesta
          const res = await axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken })
          const { accessToken, refreshToken: newRefreshToken } = res.data

          localStorage.setItem('jwt_token', accessToken)
          if (newRefreshToken) {
            localStorage.setItem('jwt_refresh_token', newRefreshToken)
          }

          apiClient.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`
          originalRequest.headers.Authorization = `Bearer ${accessToken}`

          processQueue(null, accessToken)
          return apiClient(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          // BUG-005: sync Zustand store + replace umjesto href + idempotent guard
          forceLogout()
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else {
        // Ako nema refresh tokena, odjavi korisnika
        forceLogout()
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient

