import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HostVerification from '../../../components/host/HostVerification'

vi.mock('../../../api/verificationApi', () => ({
  verificationApi: {
    getByUser: vi.fn(),
    submit: vi.fn()
  }
}))
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => ({ user: { id: 7 } })
}))
const showToast = vi.fn()
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast })
}))

describe('HostVerification', () => {
  beforeEach(() => { showToast.mockClear() })

  it('prikazuje formu kad nema verifikacija', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getByUser.mockResolvedValueOnce([])
    render(<HostVerification />)
    expect(await screen.findByPlaceholderText(/Broj dokumenta/)).toBeInTheDocument()
  })

  it('prikazuje verified badge ako postoji APPROVED', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getByUser.mockResolvedValueOnce([{ status: 'APPROVED' }])
    render(<HostVerification />)
    expect(await screen.findByText(/Verifikovan/)).toBeInTheDocument()
  })

  it('BUG B — error message koji je objekt se normalizira u string', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getByUser.mockResolvedValueOnce([])
    verificationApi.submit.mockRejectedValueOnce({
      response: { data: { message: { userId: 'User ID je obavezan' } } }
    })
    render(<HostVerification />)
    const input = await screen.findByPlaceholderText(/Broj dokumenta/)
    await userEvent.type(input, '1234567890')
    await userEvent.click(screen.getByText(/Pošalji na verifikaciju/))
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'error',
          message: expect.stringContaining('User ID je obavezan')
        })
      )
    })
  })

  it('uspjesan submit prikazuje success toast', async () => {
    const { verificationApi } = await import('../../../api/verificationApi')
    verificationApi.getByUser.mockResolvedValueOnce([])
    verificationApi.submit.mockResolvedValueOnce({ id: 1 })
    render(<HostVerification />)
    const input = await screen.findByPlaceholderText(/Broj dokumenta/)
    await userEvent.type(input, '1234567890')
    await userEvent.click(screen.getByText(/Pošalji na verifikaciju/))
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith(
        expect.objectContaining({ type: 'success' })
      )
    })
  })
})
