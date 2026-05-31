import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HostProblemReports from '../../../components/host/HostProblemReports'

vi.mock('../../../api/problemReportApi', () => ({
  problemReportApi: {
    getByHost: vi.fn(),
    updateStatus: vi.fn()
  }
}))
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => ({ user: { id: 3 } })
}))
const showToast = vi.fn()
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast })
}))

describe('HostProblemReports', () => {
  beforeEach(() => { showToast.mockClear() })

  it('ne renderuje nista kad nema prijava', async () => {
    const { problemReportApi } = await import('../../../api/problemReportApi')
    problemReportApi.getByHost.mockResolvedValueOnce([])
    const { container } = render(<HostProblemReports />)
    await waitFor(() => {
      expect(container.querySelector('h2')).toBeNull()
    })
  })

  it('prikazuje broj prijava u naslovu', async () => {
    const { problemReportApi } = await import('../../../api/problemReportApi')
    problemReportApi.getByHost.mockResolvedValueOnce([
      { id: 1, reservationId: 9, category: 'BUKA', description: 'galama', status: 'REPORTED' },
      { id: 2, reservationId: 9, category: 'CISTOCA', description: 'prljavo', status: 'IN_PROGRESS' }
    ])
    render(<HostProblemReports />)
    expect(await screen.findByText(/Prijave problema \(2\)/)).toBeInTheDocument()
  })

  it('promjena statusa zove updateStatus + success toast', async () => {
    const { problemReportApi } = await import('../../../api/problemReportApi')
    problemReportApi.getByHost.mockResolvedValueOnce([
      { id: 1, reservationId: 9, category: 'BUKA', description: 'x', status: 'REPORTED' }
    ])
    problemReportApi.updateStatus.mockResolvedValueOnce({})
    problemReportApi.getByHost.mockResolvedValueOnce([
      { id: 1, reservationId: 9, category: 'BUKA', description: 'x', status: 'RESOLVED' }
    ])
    render(<HostProblemReports />)
    const select = await screen.findByRole('combobox')
    await userEvent.selectOptions(select, 'RESOLVED')
    await waitFor(() => {
      expect(problemReportApi.updateStatus).toHaveBeenCalledWith(1, 'RESOLVED')
      expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ type: 'success' }))
    })
  })
})
