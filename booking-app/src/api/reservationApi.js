import apiClient from './apiClient'

export const reservationApi = {
  getAll: async (page = 0, size = 10) => {
    const response = await apiClient.get('/api/reservations', { params: { page, size } })
    return response.data
  },

  getById: async (id) => {
    const response = await apiClient.get(`/api/reservations/${id}`)
    return response.data
  },

  create: async (reservationData) => {
    const response = await apiClient.post('/api/reservations', reservationData)
    return response.data
  },

  update: async (id, reservationData) => {
    const response = await apiClient.put(`/api/reservations/${id}`, reservationData)
    return response.data
  },

  /**
   * Otkaži rezervaciju — backend endpoint /cancel pokreće cancellation
   * policy provjeru (7-day pravilo) i ako je sve OK, status → CANCELLED.
   * GUEST/HOST/ADMIN smiju zvati (DELETE endpoint je samo za ADMIN-a).
   */
  cancel: async (id) => {
    const response = await apiClient.put(`/api/reservations/${id}/cancel`)
    return response.data
  },

  getByGuestId: async (guestId) => {
    const response = await apiClient.get(`/api/reservations/guest/${guestId}`)
    return response.data
  },

  getByHostId: async (hostId) => {
    const response = await apiClient.get(`/api/reservations/host/${hostId}`)
    return response.data
  },

  // Public — guest moze dohvatiti zauzete datume za prikaz na kalendaru
  getOccupiedDates: async (propertyId) => {
    const response = await apiClient.get(`/api/reservations/property/${propertyId}/occupied-dates`)
    return response.data
  }
}

