import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import GuestDatePicker from '../../../components/reservations/GuestDatePicker'

vi.mock('../../../api/propertyApi', () => ({
  propertyApi: {
    getCalendarBlocks: vi.fn().mockResolvedValue([])
  }
}))
vi.mock('../../../api/reservationApi', () => ({
  reservationApi: {
    getOccupiedDates: vi.fn().mockResolvedValue([])
  }
}))

describe('GuestDatePicker', () => {
  beforeEach(async () => {
    const { propertyApi } = await import('../../../api/propertyApi')
    const { reservationApi } = await import('../../../api/reservationApi')
    propertyApi.getCalendarBlocks.mockResolvedValue([])
    reservationApi.getOccupiedDates.mockResolvedValue([])
  })

  it('renderuje calendar grid (42 cell-a)', () => {
    const { container } = render(<GuestDatePicker propertyId={5} hostId={3} onChange={() => {}} />)
    expect(container.querySelectorAll('.cal-day').length).toBe(42)
  })

  it('navigacija na sljedeci mjesec', async () => {
    render(<GuestDatePicker propertyId={5} hostId={3} onChange={() => {}} />)
    const next = screen.getByText('›')
    const before = screen.getByText(/\d{4}/).textContent
    await userEvent.click(next)
    const after = screen.getByText(/\d{4}/).textContent
    expect(before).not.toBe(after)
  })

  it('dohvata occupied-dates pri mount-u', async () => {
    const { reservationApi } = await import('../../../api/reservationApi')
    render(<GuestDatePicker propertyId={42} hostId={3} onChange={() => {}} />)
    await waitFor(() => {
      expect(reservationApi.getOccupiedDates).toHaveBeenCalledWith(42)
    })
  })

  it('prikazuje legend (Proslo, Zauzeto, Vas odabir)', () => {
    render(<GuestDatePicker propertyId={1} hostId={3} onChange={() => {}} />)
    expect(screen.getByText(/Prošlo/)).toBeInTheDocument()
    expect(screen.getByText(/Zauzeto/)).toBeInTheDocument()
    expect(screen.getByText(/Vaš odabir/)).toBeInTheDocument()
  })

  it('inicijalno value preset-uje rangeStart/End', () => {
    render(
      <GuestDatePicker
        propertyId={1}
        hostId={3}
        value={{ checkIn: '2030-06-10', checkOut: '2030-06-15' }}
        onChange={() => {}}
      />
    )
    expect(screen.getByText(/Odabrano:/)).toBeInTheDocument()
    expect(screen.getByText(/2030-06-10/)).toBeInTheDocument()
  })
})
