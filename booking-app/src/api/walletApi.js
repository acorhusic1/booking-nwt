import apiClient from './apiClient'

export const walletApi = {
  getByUserId: async (userId) => {
    const response = await apiClient.get(`/api/wallets/user/${userId}`)
    return response.data
  },

  create: async (userId, currency = 'BAM', balance = 0) => {
    const response = await apiClient.post('/api/wallets', { userId, currency, balance })
    return response.data
  },

  /**
   * Top-up wallet (deposit). Backend prima amount kao query param.
   */
  deposit: async (walletId, amount) => {
    const response = await apiClient.post(
      `/api/wallets/${walletId}/deposit`,
      null,
      { params: { amount } }
    )
    return response.data
  },

  withdraw: async (walletId, amount) => {
    const response = await apiClient.post(
      `/api/wallets/${walletId}/withdraw`,
      null,
      { params: { amount } }
    )
    return response.data
  },

  // ── Stripe Checkout (real payment processor) ─────────────────────
  createStripeCheckout: async (walletId, amount) => {
    const response = await apiClient.post('/api/stripe/checkout-session', {
      walletId, amount
    })
    return response.data  // { sessionId, url }
  },

  verifyStripeSession: async (sessionId) => {
    const response = await apiClient.get(`/api/stripe/verify-session/${sessionId}`)
    return response.data  // updated wallet
  }
}
