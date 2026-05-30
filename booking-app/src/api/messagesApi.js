import apiClient from './apiClient'

/**
 * F8 — Konverzacije i poruke.
 * Backend: notification-service /api/conversations + /api/messages.
 */
export const messagesApi = {
  // ── Konverzacije ────────────────────────────────────
  getByGuest: async (guestId) => {
    const response = await apiClient.get(`/api/conversations/guest/${guestId}`)
    return response.data
  },

  getByHost: async (hostId) => {
    const response = await apiClient.get(`/api/conversations/host/${hostId}`)
    return response.data
  },

  createConversation: async (guestId, hostId, propertyId, reservationId) => {
    const response = await apiClient.post('/api/conversations', {
      guestId, hostId, propertyId, reservationId
    })
    return response.data
  },

  // ── Poruke ──────────────────────────────────────────
  getMessages: async (conversationId) => {
    const response = await apiClient.get(`/api/messages/conversation/${conversationId}`)
    return response.data
  },

  sendMessage: async (conversationId, senderId, content) => {
    const response = await apiClient.post('/api/messages', {
      conversationId, senderId, content
    })
    return response.data
  },

  markAsRead: async (messageId) => {
    const response = await apiClient.put(`/api/messages/${messageId}/read`)
    return response.data
  }
}
