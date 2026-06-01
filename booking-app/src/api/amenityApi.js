import apiClient from './apiClient'

/**
 * F1 — Sadrzaji (WiFi, Parking, Klima, ...). Backend: property-service /api/amenities
 */
export const amenityApi = {
  getAll: async () => {
    const r = await apiClient.get('/api/amenities')
    return r.data
  }
}
