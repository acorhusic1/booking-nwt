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
  },

  // F4 — Dinamicke cijene (PricingRule)
  getPricing: async (propertyId) => {
    const response = await apiClient.get(`/api/properties/${propertyId}/pricing`)
    return response.data
  },

  updatePricing: async (propertyId, pricingData) => {
    const response = await apiClient.put(`/api/properties/${propertyId}/pricing`, pricingData)
    return response.data
  },

  // F15 — Sezonska pravila
  getSeasonalRules: async (propertyId) => {
    const response = await apiClient.get(`/api/properties/${propertyId}/seasonal-rules`)
    return response.data
  },

  addSeasonalRule: async (propertyId, rule) => {
    const response = await apiClient.post(`/api/properties/${propertyId}/seasonal-rules`, rule)
    return response.data
  },

  deleteSeasonalRule: async (propertyId, ruleId) => {
    await apiClient.delete(`/api/properties/${propertyId}/seasonal-rules/${ruleId}`)
  },

  // F3 — Kalendar dostupnosti (CalendarBlock)
  getCalendarBlocks: async (propertyId) => {
    const response = await apiClient.get(`/api/properties/${propertyId}/calendar-blocks`)
    return response.data
  },

  addCalendarBlock: async (propertyId, block) => {
    const response = await apiClient.post(`/api/properties/${propertyId}/calendar-blocks`, block)
    return response.data
  },

  deleteCalendarBlock: async (propertyId, blockId) => {
    await apiClient.delete(`/api/properties/${propertyId}/calendar-blocks/${blockId}`)
  }
}

