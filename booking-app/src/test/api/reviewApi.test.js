import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

import apiClient from '../../api/apiClient'
import { reviewApi } from '../../api/reviewApi'

describe('reviewApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getByProperty hita ispravan URL', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [{ id: 1 }] })
    const res = await reviewApi.getByProperty(5)
    expect(apiClient.get).toHaveBeenCalledWith('/api/reviews/property/5')
    expect(res).toEqual([{ id: 1 }])
  })

  it('getByGuest hita guest endpoint', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await reviewApi.getByGuest(7)
    expect(apiClient.get).toHaveBeenCalledWith('/api/reviews/guest/7')
  })

  it('create POST-uje review podatke', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 9 } })
    const review = { reservationId: 1, guestId: 7, propertyId: 5, hostId: 3, ratingCleanliness: 5 }
    const out = await reviewApi.create(review)
    expect(apiClient.post).toHaveBeenCalledWith('/api/reviews', review)
    expect(out.id).toBe(9)
  })

  it('addReply PUT-uje na /reply', async () => {
    apiClient.put.mockResolvedValueOnce({ data: { id: 1, hostReply: 'hvala' } })
    await reviewApi.addReply(1, 'hvala')
    expect(apiClient.put).toHaveBeenCalledWith('/api/reviews/1/reply', { reply: 'hvala' })
  })
})
