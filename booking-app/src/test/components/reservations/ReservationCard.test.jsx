import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ReservationCard from '../../../components/reservations/ReservationCard'

vi.mock('../../../api/paymentApi', () => ({
  paymentApi: { getByReservationId: vi.fn().mockResolvedValue([]) }
}))
vi.mock('../../../api/reservationApi', () => ({
  reservationApi: { cancel: vi.fn().mockResolvedValue({}) }
}))
vi.mock('../../../api/messagesApi', () => ({
  messagesApi: { createConversation: vi.fn().mockResolvedValue({ id: 42 }) }
}))
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => ({ user: { id: 7, role: 'GUEST' } })
}))
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast: vi.fn() })
}))

function wrap(ui) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

const baseRes = {
  id: 99, propertyId: 5, guestId: 7, hostId: 3,
  checkIn: '2026-08-01', checkOut: '2026-08-05',
  numGuests: 2, totalPrice: 400, status: 'CONFIRMED'
}

describe('ReservationCard', () => {
  it('prikazuje osnovne info', () => {
    wrap(<ReservationCard reservation={baseRes} />)
    expect(screen.getByText(/Rezervacija #99/)).toBeInTheDocument()
    expect(screen.getByText(/CONFIRMED/)).toBeInTheDocument()
    expect(screen.getByText(/400.00 BAM/)).toBeInTheDocument()
  })

  it('klik na karticu expanduje i fetcha placanja', async () => {
    const { paymentApi } = await import('../../../api/paymentApi')
    wrap(<ReservationCard reservation={baseRes} />)
    await userEvent.click(screen.getByText(/Rezervacija #99/))
    await waitFor(() => {
      expect(paymentApi.getByReservationId).toHaveBeenCalledWith(99)
    })
  })

  it('CANCELLED rezervacija ne prikazuje Otkazi dugme', async () => {
    wrap(<ReservationCard reservation={{ ...baseRes, status: 'CANCELLED' }} />)
    await userEvent.click(screen.getByText(/Rezervacija #99/))
    expect(screen.queryByText(/Otkazi rezervaciju/)).toBeNull()
  })

  it('GUEST vidi Posalji poruku domacinu dugme', async () => {
    wrap(<ReservationCard reservation={baseRes} />)
    await userEvent.click(screen.getByText(/Rezervacija #99/))
    expect(await screen.findByText(/Posalji poruku domacinu|poruku domaćinu/)).toBeInTheDocument()
  })
})
