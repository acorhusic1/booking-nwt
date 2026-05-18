import apiClient from './apiClient'

/**
 * Payment API klijent. Spojeno:
 *   - Ahmed: getAll/getById/create/getWallet/getTransactions
 *   - Benjamin (Saga UI): getByReservationId/getByGuestId/refund
 */
export const paymentApi = {
  // ── Ahmed (admin/host pregled) ──
  getAll: async (page = 0, size = 10) => {
    const response = await apiClient.get('/api/payments', { params: { page, size } })
    return response.data
  },

  getById: async (id) => {
    const response = await apiClient.get(`/api/payments/${id}`)
    return response.data
  },

  create: async (paymentData) => {
    const response = await apiClient.post('/api/payments', paymentData)
    return response.data
  },

  getWallet: async (userId) => {
    const response = await apiClient.get(`/api/wallets/user/${userId}`)
    return response.data
  },

  getTransactions: async (walletId) => {
    const response = await apiClient.get(`/api/wallets/${walletId}/transactions`)
    return response.data
  },

  // ── Benjamin (Saga UI — Dashboard expand kartica) ──
  /**
   * Dohvati sva plaćanja vezana za rezervaciju — koristi se da se vidi
   * šta je Saga uradila (status COMPLETED ili FAILED).
   */
  getByReservationId: async (reservationId) => {
    const response = await apiClient.get(`/api/payments/reservation/${reservationId}`)
    return response.data
  },

  getByGuestId: async (guestId) => {
    const response = await apiClient.get(`/api/payments/guest/${guestId}`)
    return response.data
  },

  /**
   * Refund: tehnički GUEST/ADMIN endpoint na backendu. Trenutno se ne
   * koristi iz UI-a — Saga sama radi kompenzaciju kad payment padne.
   */
  refund: async (paymentId) => {
    const response = await apiClient.post(`/api/payments/${paymentId}/refund`)
    return response.data
  }
}
