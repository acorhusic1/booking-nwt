import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ReviewModal from '../../../components/reservations/ReviewModal'

vi.mock('../../../api/reviewApi', () => ({
  reviewApi: { create: vi.fn() }
}))
const showToast = vi.fn()
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast })
}))

const reservation = { id: 99, guestId: 7, propertyId: 5, hostId: 3 }

describe('ReviewModal', () => {
  beforeEach(() => { showToast.mockClear() })

  it('prikazuje naslov sa rezervacija ID', () => {
    render(<ReviewModal open onClose={() => {}} reservation={reservation} />)
    expect(screen.getByText(/Ocijeni rezervaciju #99/)).toBeInTheDocument()
  })

  it('prikazuje 5 kategorija ocjena', () => {
    render(<ReviewModal open onClose={() => {}} reservation={reservation} />)
    expect(screen.getByText(/Čistoća/)).toBeInTheDocument()
    expect(screen.getByText(/Lokacija/)).toBeInTheDocument()
    expect(screen.getByText(/Komunikacija/)).toBeInTheDocument()
    expect(screen.getByText(/Vrijednost za novac/)).toBeInTheDocument()
    expect(screen.getByText(/Tačnost opisa/)).toBeInTheDocument()
  })

  it('Posalji recenziju dugme disabled dok sve kategorije nisu ocijenjene', () => {
    render(<ReviewModal open onClose={() => {}} reservation={reservation} />)
    const submit = screen.getByText(/Pošalji recenziju/)
    expect(submit).toBeDisabled()
  })

  it('uspjesan submit poziva API + onSubmitted + onClose', async () => {
    const { reviewApi } = await import('../../../api/reviewApi')
    reviewApi.create.mockResolvedValueOnce({ id: 1 })
    const onClose = vi.fn()
    const onSubmitted = vi.fn()
    render(
      <ReviewModal open onClose={onClose} onSubmitted={onSubmitted} reservation={reservation} />
    )
    // Modal je portal u document.body, container.querySelectorAll ga ne vidi
    const allStars = document.querySelectorAll('.star')
    // 5 kategorija * 5 zvjezdica = 25 — kliknemo svaku zadnju (5-tu)
    for (let cat = 0; cat < 5; cat++) {
      await userEvent.click(allStars[cat * 5 + 4])
    }
    await userEvent.click(screen.getByText(/Pošalji recenziju/))
    await waitFor(() => {
      expect(reviewApi.create).toHaveBeenCalled()
      expect(onSubmitted).toHaveBeenCalled()
      expect(onClose).toHaveBeenCalled()
    })
  })
})
