import apiClient from './apiClient'

export const propertyApi = {
  getAll: async (page = 0, size = 10, city = '') => {
    const params = { page, size }
    if (city) params.city = city
    const response = await apiClient.get('/api/properties', { params })
    return response.data
  },

  getById: async (id) => {
    const response = await apiClient.get(`/api/properties/${id}`)
    return response.data
  },

  create: async (propertyData) => {
    const response = await apiClient.post('/api/properties', propertyData)
    return response.data
  },

  update: async (id, propertyData) => {
    const response = await apiClient.put(`/api/properties/${id}`, propertyData)
    return response.data
  },

  delete: async (id) => {
    await apiClient.delete(`/api/properties/${id}`)
  },

  search: async (city, startDate, endDate) => {
    const response = await apiClient.get('/api/properties/search', {
      params: { city, startDate, endDate }
    })
    return response.data
  },

  getByHostId: async (hostId) => {
    const response = await apiClient.get(`/api/properties/host/${hostId}`)
    return response.data
  },

  getImages: async (propertyId) => {
    const response = await apiClient.get(`/api/properties/${propertyId}/images`)
    return response.data
  },

  addImage: async (propertyId, imageData) => {
    const response = await apiClient.post(`/api/properties/${propertyId}/images`, imageData)
    return response.data
  }
}

