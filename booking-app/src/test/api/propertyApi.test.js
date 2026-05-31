import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { propertyApi } from '../../api/propertyApi'

describe('propertyApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getAll bez city - bez params.city', async () => {
    apiClient.get.mockResolvedValueOnce({ data: { content: [] } })
    await propertyApi.getAll(2, 5)
    expect(apiClient.get).toHaveBeenCalledWith('/api/properties', { params: { page: 2, size: 5 } })
  })

  it('getAll s city dodaje params.city', async () => {
    apiClient.get.mockResolvedValueOnce({ data: { content: [] } })
    await propertyApi.getAll(0, 10, 'Sarajevo')
    expect(apiClient.get).toHaveBeenCalledWith('/api/properties', {
      params: { page: 0, size: 10, city: 'Sarajevo' }
    })
  })

  it('search prosljedi datume', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await propertyApi.search('Mostar', '2026-08-01', '2026-08-05')
    expect(apiClient.get).toHaveBeenCalledWith('/api/properties/search', {
      params: { city: 'Mostar', startDate: '2026-08-01', endDate: '2026-08-05' }
    })
  })

  it('addCalendarBlock POST-uje na pravi URL', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1 } })
    const block = { startDate: '2026-08-01', endDate: '2026-08-05', reason: 'Renoviranje' }
    await propertyApi.addCalendarBlock(7, block)
    expect(apiClient.post).toHaveBeenCalledWith('/api/properties/7/calendar-blocks', block)
  })

  it('deleteCalendarBlock DELETE-uje na pravi URL', async () => {
    apiClient.delete.mockResolvedValueOnce({})
    await propertyApi.deleteCalendarBlock(7, 99)
    expect(apiClient.delete).toHaveBeenCalledWith('/api/properties/7/calendar-blocks/99')
  })

  it('getPricing i updatePricing', async () => {
    apiClient.get.mockResolvedValueOnce({ data: { basePrice: 100 } })
    apiClient.put.mockResolvedValueOnce({ data: { basePrice: 120 } })
    await propertyApi.getPricing(5)
    await propertyApi.updatePricing(5, { basePrice: 120 })
    expect(apiClient.get).toHaveBeenCalledWith('/api/properties/5/pricing')
    expect(apiClient.put).toHaveBeenCalledWith('/api/properties/5/pricing', { basePrice: 120 })
  })

  it('addSeasonalRule + deleteSeasonalRule', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1 } })
    apiClient.delete.mockResolvedValueOnce({})
    await propertyApi.addSeasonalRule(5, { name: 'Ljeto', priceModifierPct: 30 })
    await propertyApi.deleteSeasonalRule(5, 1)
    expect(apiClient.post).toHaveBeenCalledWith('/api/properties/5/seasonal-rules', expect.any(Object))
    expect(apiClient.delete).toHaveBeenCalledWith('/api/properties/5/seasonal-rules/1')
  })
})
