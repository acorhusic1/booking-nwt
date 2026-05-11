import apiClient from './apiClient'

export const walletApi = {
  getByUserId: async (userId) => {
    const response = await apiClient.get(`/api/wallets/user/${userId}`)
    return response.data
  }
}
