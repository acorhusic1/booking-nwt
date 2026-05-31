import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { notificationApi } from '../../api/notificationApi'

describe('notificationApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getByUserId', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await notificationApi.getByUserId(7)
    expect(apiClient.get).toHaveBeenCalledWith('/api/notifications/user/7')
  })

  it('countUnread', async () => {
    apiClient.get.mockResolvedValueOnce({ data: 3 })
    const n = await notificationApi.countUnread(7)
    expect(apiClient.get).toHaveBeenCalledWith('/api/notifications/user/7/unread/count')
    expect(n).toBe(3)
  })

  it('markAsRead PUT-uje na /read', async () => {
    apiClient.put.mockResolvedValueOnce({ data: { id: 5, isRead: true } })
    await notificationApi.markAsRead(5)
    expect(apiClient.put).toHaveBeenCalledWith('/api/notifications/5/read')
  })
})
