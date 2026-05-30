import apiClient from './apiClient'

/**
 * Recenzije (F7). Backend: property-service /api/reviews.
 * 5 kategorija ocjena (1-5): cleanliness, location, communication, value, accuracy.
 */
export const reviewApi = {
  getByProperty: async (propertyId) => {
    const response = await apiClient.get(`/api/reviews/property/${propertyId}`)
    return response.data
  },

  getByGuest: async (guestId) => {
    const response = await apiClient.get(`/api/reviews/guest/${guestId}`)
    return response.data
  },

  create: async (reviewData) => {
    const response = await apiClient.post('/api/reviews', reviewData)
    return response.data
  },

  // HOST odgovara na recenziju
  addReply: async (reviewId, reply) => {
    const response = await apiClient.put(`/api/reviews/${reviewId}/reply`, { reply })
    return response.data
  }
}
