import apiClient from './apiClient'

/**
 * Verifikacija identiteta domaćina (F16).
 * Backend: user-service.
 *   /api/users/{userId}/verifications — host submit + pregled svojih
 *   /api/verifications — admin pregled svih + approve/reject
 */
export const verificationApi = {
  // Host — svoje verifikacije
  getByUser: async (userId) => {
    const response = await apiClient.get(`/api/users/${userId}/verifications`)
    return response.data
  },

  submit: async (userId, documentType, documentNumber) => {
    const response = await apiClient.post(`/api/users/${userId}/verifications`, {
      documentType, documentNumber
    })
    return response.data
  },

  // Admin
  getAll: async () => {
    const response = await apiClient.get('/api/verifications')
    return response.data
  },

  updateStatus: async (id, status, verifiedBy) => {
    const response = await apiClient.patch(`/api/verifications/${id}/status`, null, {
      params: { status, verifiedBy }
    })
    return response.data
  }
}
