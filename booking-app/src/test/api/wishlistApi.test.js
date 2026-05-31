import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { wishlistApi } from '../../api/wishlistApi'

describe('wishlistApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getByGuest', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await wishlistApi.getByGuest(7)
    expect(apiClient.get).toHaveBeenCalledWith('/api/wishlists/guest/7')
  })

  it('create salje guestId i name', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1 } })
    await wishlistApi.create(7, 'Moja lista')
    expect(apiClient.post).toHaveBeenCalledWith('/api/wishlists', { guestId: 7, name: 'Moja lista' })
  })

  it('addItem salje samo propertyId', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 99 } })
    await wishlistApi.addItem(1, 50)
    expect(apiClient.post).toHaveBeenCalledWith('/api/wishlists/1/items', { propertyId: 50 })
  })

  it('removeItem DELETE-uje', async () => {
    apiClient.delete.mockResolvedValueOnce({})
    await wishlistApi.removeItem(1, 99)
    expect(apiClient.delete).toHaveBeenCalledWith('/api/wishlists/1/items/99')
  })

  it('getItems', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await wishlistApi.getItems(1)
    expect(apiClient.get).toHaveBeenCalledWith('/api/wishlists/1/items')
  })
})
