import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { promoApi } from '../../api/promoApi'

describe('promoApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('validateByCode upper-case kod', async () => {
    apiClient.get.mockResolvedValueOnce({ data: { id: 1, code: 'LJETO2026' } })
    const res = await promoApi.validateByCode('LJETO2026')
    expect(apiClient.get).toHaveBeenCalledWith('/api/promo-codes/code/LJETO2026')
    expect(res.code).toBe('LJETO2026')
  })

  it('validateByCode URL-encode-uje specijalne znakove', async () => {
    apiClient.get.mockResolvedValueOnce({ data: null })
    await promoApi.validateByCode('LJET O2026')
    expect(apiClient.get).toHaveBeenCalledWith('/api/promo-codes/code/LJET%20O2026')
  })
})
