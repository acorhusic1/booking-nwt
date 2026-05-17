import apiClient from './apiClient'

export const notificationApi = {
  getByUserId: async (userId) => {
    const response = await apiClient.get(`/api/notifications/user/${userId}`)
    return response.data
  },

  countUnread: async (userId) => {
    const response = await apiClient.get(`/api/notifications/user/${userId}/unread/count`)
    return response.data
  },

  markAsRead: async (id) => {
    const response = await apiClient.put(`/api/notifications/${id}/read`)
    return response.data
  }
}
