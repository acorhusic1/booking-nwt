import apiClient from './apiClient'

export const paymentApi = {
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
  }
}
