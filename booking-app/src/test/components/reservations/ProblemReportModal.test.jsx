import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ProblemReportModal from '../../../components/reservations/ProblemReportModal'

vi.mock('../../../api/problemReportApi', () => ({
  problemReportApi: { create: vi.fn() }
}))
const showToast = vi.fn()
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast })
}))

const reservation = { id: 88 }

describe('ProblemReportModal', () => {
  beforeEach(() => { showToast.mockClear() })

  it('naslov prikazuje rezervaciju ID', () => {
    render(<ProblemReportModal open onClose={() => {}} reservation={reservation} reporterId={7} />)
    expect(screen.getByText(/Prijava problema/)).toBeInTheDocument()
    expect(screen.getByText(/#88/)).toBeInTheDocument()
  })

  it('submit dugme disabled za opis kraci od 10 znakova', async () => {
    render(<ProblemReportModal open onClose={() => {}} reservation={reservation} reporterId={7} />)
    const submit = screen.getByText(/Pošalji prijavu/)
    expect(submit).toBeDisabled()
    const textarea = screen.getByPlaceholderText(/Opišite problem/)
    await userEvent.type(textarea, 'kratko')
    expect(submit).toBeDisabled()
  })

  it('uspjesan submit poziva API + onSubmitted + onClose', async () => {
    const { problemReportApi } = await import('../../../api/problemReportApi')
    problemReportApi.create.mockResolvedValueOnce({ id: 1 })
    const onClose = vi.fn()
    const onSubmitted = vi.fn()
    render(
      <ProblemReportModal open onClose={onClose} onSubmitted={onSubmitted}
        reservation={reservation} reporterId={7} />
    )
    const textarea = screen.getByPlaceholderText(/Opišite problem/)
    await userEvent.type(textarea, 'Voda curi iz slavine vec dva dana.')
    await userEvent.click(screen.getByText(/Pošalji prijavu/))
    await waitFor(() => {
      expect(problemReportApi.create).toHaveBeenCalledWith(
        88, 7, expect.any(String), expect.stringContaining('Voda curi')
      )
      expect(onSubmitted).toHaveBeenCalled()
      expect(onClose).toHaveBeenCalled()
    })
  })

  it('renderuje 6 kategorija u select-u', () => {
    render(
      <ProblemReportModal open onClose={() => {}} reservation={reservation} reporterId={7} />
    )
    // Modal kroz portal u document.body — coristimo document.querySelectorAll
    expect(document.querySelectorAll('select option').length).toBe(6)
  })
})
