import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: {
    get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), patch: vi.fn()
  }
}))

import apiClient from '../../api/apiClient'
import { verificationApi } from '../../api/verificationApi'

describe('verificationApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('getByUser dohvati za usera', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await verificationApi.getByUser(7)
    expect(apiClient.get).toHaveBeenCalledWith('/api/users/7/verifications')
  })

  it('submit BEZ userId u body-u (BUG B fix)', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1 } })
    await verificationApi.submit(7, 'LIČNA_KARTA', '1234567890')
    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/users/7/verifications',
      { documentType: 'LIČNA_KARTA', documentNumber: '1234567890' }
    )
  })

  it('updateStatus prosljedi status + verifiedBy kao query params', async () => {
    apiClient.patch.mockResolvedValueOnce({ data: { id: 1 } })
    await verificationApi.updateStatus(1, 'APPROVED', 99)
    expect(apiClient.patch).toHaveBeenCalledWith(
      '/api/verifications/1/status', null,
      { params: { status: 'APPROVED', verifiedBy: 99 } }
    )
  })

  it('getAll za admina', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await verificationApi.getAll()
    expect(apiClient.get).toHaveBeenCalledWith('/api/verifications')
  })
})
