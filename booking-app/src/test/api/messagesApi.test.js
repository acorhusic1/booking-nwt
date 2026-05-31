import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { messagesApi } from '../../api/messagesApi'

describe('messagesApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getByGuest hita guest endpoint', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await messagesApi.getByGuest(7)
    expect(apiClient.get).toHaveBeenCalledWith('/api/conversations/guest/7')
  })

  it('getByHost hita host endpoint', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await messagesApi.getByHost(3)
    expect(apiClient.get).toHaveBeenCalledWith('/api/conversations/host/3')
  })

  it('createConversation salje sve 4 ID-a', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 42 } })
    const conv = await messagesApi.createConversation(7, 3, 5, 99)
    expect(apiClient.post).toHaveBeenCalledWith('/api/conversations', {
      guestId: 7, hostId: 3, propertyId: 5, reservationId: 99
    })
    expect(conv.id).toBe(42)
  })

  it('sendMessage salje sender + content', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1, content: 'hi' } })
    await messagesApi.sendMessage(42, 7, 'hi')
    expect(apiClient.post).toHaveBeenCalledWith('/api/messages', {
      conversationId: 42, senderId: 7, content: 'hi'
    })
  })

  it('markAsRead PUT-uje na /read', async () => {
    apiClient.put.mockResolvedValueOnce({ data: {} })
    await messagesApi.markAsRead(1)
    expect(apiClient.put).toHaveBeenCalledWith('/api/messages/1/read')
  })

  it('getMessages dohvati po conversation ID', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [{ id: 1 }] })
    const out = await messagesApi.getMessages(42)
    expect(apiClient.get).toHaveBeenCalledWith('/api/messages/conversation/42')
    expect(out.length).toBe(1)
  })
})
