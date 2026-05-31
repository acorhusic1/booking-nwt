import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../../api/apiClient', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

import apiClient from '../../api/apiClient'
import { problemReportApi } from '../../api/problemReportApi'

describe('problemReportApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('create salje cetiri obavezna polja', async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1 } })
    await problemReportApi.create(5, 7, 'BUKA', 'puno galame')
    expect(apiClient.post).toHaveBeenCalledWith('/api/problem-reports', {
      reservationId: 5, reporterId: 7, category: 'BUKA', description: 'puno galame'
    })
  })

  it('getByReservation', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await problemReportApi.getByReservation(5)
    expect(apiClient.get).toHaveBeenCalledWith('/api/problem-reports/reservation/5')
  })

  it('getByHost (BUG 5)', async () => {
    apiClient.get.mockResolvedValueOnce({ data: [] })
    await problemReportApi.getByHost(3)
    expect(apiClient.get).toHaveBeenCalledWith('/api/problem-reports/host/3')
  })

  it('updateStatus salje status kao query param', async () => {
    apiClient.put.mockResolvedValueOnce({ data: { id: 1 } })
    await problemReportApi.updateStatus(1, 'RESOLVED')
    expect(apiClient.put).toHaveBeenCalledWith(
      '/api/problem-reports/1/status', null,
      { params: { status: 'RESOLVED' } }
    )
  })
})
