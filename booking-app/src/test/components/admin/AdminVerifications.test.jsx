import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AdminVerifications from '../../../components/admin/AdminVerifications'

vi.mock('../../../api/verificationApi', () => ({
  verificationApi: {
    getAll: vi.fn(),
    updateStatus: vi.fn()
  }
}))
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => ({ user: { id: 1 } })
}))
const showToast = vi.fn()
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast })
}))

describe('AdminVerifications', () => {
  beforeEach(() => { showToast.mockClear() })

  it('prazan state', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getAll.mockResolvedValueOnce([])
    render(<AdminVerifications />)
    expect(await screen.findByText(/Nema zahtjeva za verifikaciju/)).toBeInTheDocument()
  })

  it('prikazuje listu zahtjeva', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getAll.mockResolvedValueOnce([
      { id: 1, userId: 7, documentType: 'LIČNA_KARTA', documentNumber: '12345', status: 'PENDING' }
    ])
    render(<AdminVerifications />)
    expect(await screen.findByText(/User #7/)).toBeInTheDocument()
    expect(screen.getByText(/12345/)).toBeInTheDocument()
    expect(screen.getByText(/Odobri/)).toBeInTheDocument()
    expect(screen.getByText(/Odbij/)).toBeInTheDocument()
  })

  it('APPROVED nema akcije', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getAll.mockResolvedValueOnce([
      { id: 1, userId: 7, documentType: 'PASOŠ', documentNumber: 'X1', status: 'APPROVED' }
    ])
    render(<AdminVerifications />)
    await screen.findByText(/User #7/)
    expect(screen.queryByText(/Odobri/)).toBeNull()
    expect(screen.queryByText(/Odbij/)).toBeNull()
  })

  it('Odobri poziva updateStatus(APPROVED) sa adminId', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getAll.mockResolvedValueOnce([
      { id: 5, userId: 7, documentType: 'LIČNA_KARTA', documentNumber: '12', status: 'PENDING' }
    ])
    verificationApi.updateStatus.mockResolvedValueOnce({})
    verificationApi.getAll.mockResolvedValueOnce([])
    render(<AdminVerifications />)
    await userEvent.click(await screen.findByText(/Odobri/))
    await waitFor(() => {
      expect(verificationApi.updateStatus).toHaveBeenCalledWith(5, 'APPROVED', 1)
      expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ type: 'success' }))
    })
  })
})
