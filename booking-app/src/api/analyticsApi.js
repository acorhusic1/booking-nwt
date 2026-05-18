import apiClient from './apiClient'

export const analyticsApi = {
  // Statistics endpoints
  getStatisticsByHostId: async (hostId) => {
    const response = await apiClient.get(`/api/statistics/host/${hostId}`)
    return response.data
  },
  
  getStatisticsByHostIdAndPeriod: async (hostId, year, month) => {
    const response = await apiClient.get(`/api/statistics/host/${hostId}/period`, {
      params: { year, month }
    })
    return response.data
  },

  getAllStatisticsPaginated: async (page = 0, size = 10) => {
    const response = await apiClient.get('/api/statistics/paginated', {
      params: { page, size }
    })
    return response.data
  },

  // Revenue Report endpoints
  getReportsByHostId: async (hostId) => {
    const response = await apiClient.get(`/api/reports/host/${hostId}`)
    return response.data
  },

  getDetailedHostReport: async (hostId) => {
    const response = await apiClient.get(`/api/reports/host/${hostId}/detailed`)
    return response.data
  },

  getReportsByHostIdAndYear: async (hostId, year) => {
    const response = await apiClient.get(`/api/reports/host/${hostId}/year/${year}`)
    return response.data
  },

  getReportsByPeriod: async (year, month) => {
    const response = await apiClient.get('/api/reports/period', {
      params: { year, month }
    })
    return response.data
  }
}
