import apiClient from './apiClient'

/**
 * Wishlist (F10). Backend: property-service /api/wishlists.
 * Gost ima vise imenovanih listi, svaka sadrzi stavke (propertyId).
 */
export const wishlistApi = {
  getByGuest: async (guestId) => {
    const response = await apiClient.get(`/api/wishlists/guest/${guestId}`)
    return response.data
  },

  create: async (guestId, name) => {
    const response = await apiClient.post('/api/wishlists', { guestId, name })
    return response.data
  },

  remove: async (wishlistId) => {
    await apiClient.delete(`/api/wishlists/${wishlistId}`)
  },

  getItems: async (wishlistId) => {
    const response = await apiClient.get(`/api/wishlists/${wishlistId}/items`)
    return response.data
  },

  addItem: async (wishlistId, propertyId) => {
    const response = await apiClient.post(`/api/wishlists/${wishlistId}/items`, { propertyId })
    return response.data
  },

  removeItem: async (wishlistId, itemId) => {
    await apiClient.delete(`/api/wishlists/${wishlistId}/items/${itemId}`)
  }
}
