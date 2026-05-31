import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import CalendarModal from '../../../components/host/CalendarModal'

vi.mock('../../../api/propertyApi', () => ({
  propertyApi: {
    getCalendarBlocks: vi.fn(),
    addCalendarBlock: vi.fn(),
    deleteCalendarBlock: vi.fn()
  }
}))
vi.mock('../../../api/reservationApi', () => ({
  reservationApi: {
    getOccupiedDates: vi.fn()
  }
}))
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => ({ user: { id: 3 } })
}))
vi.mock('../../../components/common/ToastProvider', () => ({
  useToast: () => ({ showToast: vi.fn() })
}))

const property = { id: 7, name: 'Test Vila' }

describe('CalendarModal', () => {
  beforeEach(async () => {
    const { propertyApi } = await import('../../../api/propertyApi')
    const { reservationApi } = await import('../../../api/reservationApi')
    propertyApi.getCalendarBlocks.mockResolvedValue([])
    reservationApi.getOccupiedDates.mockResolvedValue([])
  })

  it('ne renderuje nista kad nije open', () => {
    const { container } = render(<CalendarModal open={false} onClose={() => {}} property={property} />)
    expect(container.querySelector('.calendar-grid')).toBeNull()
  })

  it('otvoreno → naslov sa property imenom', () => {
    render(<CalendarModal open onClose={() => {}} property={property} />)
    expect(screen.getByText(/Test Vila/)).toBeInTheDocument()
  })

  it('dohvati i blokove i occupied-dates (BUG H)', async () => {
    const { propertyApi } = await import('../../../api/propertyApi')
    const { reservationApi } = await import('../../../api/reservationApi')
    render(<CalendarModal open onClose={() => {}} property={property} />)
    await waitFor(() => {
      expect(propertyApi.getCalendarBlocks).toHaveBeenCalledWith(7)
      expect(reservationApi.getOccupiedDates).toHaveBeenCalledWith(7)
    })
  })

  it('renderuje 42 day-celija', () => {
    render(<CalendarModal open onClose={() => {}} property={property} />)
    // Modal renderuje kroz React portal u document.body, ne unutar container-a
    expect(document.querySelectorAll('.cal-day').length).toBe(42)
  })

  it('prikazuje "Nema blokiranih perioda" kad nema blokova', async () => {
    render(<CalendarModal open onClose={() => {}} property={property} />)
    expect(await screen.findByText(/Nema blokiranih perioda/)).toBeInTheDocument()
  })
})
