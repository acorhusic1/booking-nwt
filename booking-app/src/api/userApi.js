import apiClient from './apiClient'

export const userApi = {
  getAll: async (page = 0, size = 10) => {
    const response = await apiClient.get('/api/users/paginated', { params: { page, size } })
    return response.data
  },

  getById: async (id) => {
    const response = await apiClient.get(`/api/users/${id}`)
    return response.data
  },

  getByEmail: async (email) => {
    const response = await apiClient.get(`/api/users/email/${email}`)
    return response.data
  },

  getDetails: async (id) => {
    const response = await apiClient.get(`/api/users/${id}/details`)
    return response.data
  },

  update: async (id, userData) => {
    const response = await apiClient.put(`/api/users/${id}`, userData)
    return response.data
  },

  patch: async (id, patchData) => {
    const response = await apiClient.patch(`/api/users/${id}`, patchData)
    return response.data
  },

  delete: async (id) => {
    await apiClient.delete(`/api/users/${id}`)
  },

  search: async (role, active, page = 0, size = 10) => {
    const params = { page, size }
    if (role) params.role = role
    if (active !== undefined) params.active = active
    const response = await apiClient.get('/api/users/search', { params })
    return response.data
  }
}
