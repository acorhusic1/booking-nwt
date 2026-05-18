import apiClient from './apiClient'

export const walletApi = {
  getByUserId: async (userId) => {
    const response = await apiClient.get(`/api/wallets/user/${userId}`)
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
  }
}
