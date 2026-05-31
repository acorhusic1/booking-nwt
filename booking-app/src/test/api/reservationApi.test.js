import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { reservationApi } from '../../api/reservationApi'

describe('reservationApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getAll prosljedi page i size', async () => {
    apiClient.get.mockResolvedValueOnce({ data: { content: [] } })
    await reservationApi.getAll(1, 20)
    expect(apiClient.get).toHaveBeenCalledWith('/api/reservations', { params: { page: 1, size: 20 } })
  })

  it('create POST-uje rezervaciju', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 9 } })
    const data = { propertyId: 5, guestId: 7, checkIn: '2026-08-01', checkOut: '2026-08-05' }
    const out = await reservationApi.create(data)
    expect(apiClient.post).toHaveBeenCalledWith('/api/reservations', data)
    expect(out.id).toBe(9)
  })

  it('cancel PUT-uje na /cancel', async () => {
    apiClient.put.mockResolvedValueOnce({ data: { id: 9, status: 'CANCELLED' } })
    await reservationApi.cancel(9)
    expect(apiClient.put).toHaveBeenCalledWith('/api/reservations/9/cancel')
  })

  it('getByHostId', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await reservationApi.getByHostId(3)
    expect(apiClient.get).toHaveBeenCalledWith('/api/reservations/host/3')
  })

  it('getOccupiedDates hita public endpoint', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [{ checkIn: '2026-08-01', checkOut: '2026-08-05' }] })
    const out = await reservationApi.getOccupiedDates(5)
    expect(apiClient.get).toHaveBeenCalledWith('/api/reservations/property/5/occupied-dates')
    expect(out.length).toBe(1)
  })
})
