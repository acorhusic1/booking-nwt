import apiClient from './apiClient'

/**
 * F17 — Prijava problema tokom boravka.
 * Backend: reservation-service /api/problem-reports.
 */
export const problemReportApi = {
  create: async (reservationId, reporterId, category, description) => {
    const response = await apiClient.post('/api/problem-reports', {
      reservationId, reporterId, category, description
    })
    return response.data
  },

  getByReservation: async (reservationId) => {
    const response = await apiClient.get(`/api/problem-reports/reservation/${reservationId}`)
    return response.data
  },

  getByReporter: async (reporterId) => {
    const response = await apiClient.get(`/api/problem-reports/reporter/${reporterId}`)
    return response.data
  },

  // BUG 5 — Host vidi prijave za svoje smještaje
  getByHost: async (hostId) => {
    const response = await apiClient.get(`/api/problem-reports/host/${hostId}`)
    return response.data
  },

  getAll: async () => {
    const response = await apiClient.get('/api/problem-reports')
    return response.data
  },

  updateStatus: async (id, status) => {
    const response = await apiClient.put(`/api/problem-reports/${id}/status`, null, {
      params: { status }
    })
    return response.data
  }
}
