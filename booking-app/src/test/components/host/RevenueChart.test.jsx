import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import RevenueChart from '../../../components/host/RevenueChart'

describe('RevenueChart', () => {
  it('empty state kad nema rezervacija', () => {
    render(<RevenueChart reservations={[]} year={2026} />)
    expect(screen.getByText(/Nema rezervacija/)).toBeInTheDocument()
    expect(screen.getByText('0')).toBeInTheDocument()
  })

  it('zbraja samo CONFIRMED/ACTIVE/COMPLETED', () => {
    const list = [
      { checkIn: '2026-08-01', status: 'CONFIRMED', totalPrice: 200 },
      { checkIn: '2026-08-15', status: 'CANCELLED', totalPrice: 999 },
      { checkIn: '2026-08-20', status: 'CREATED', totalPrice: 100 },
      { checkIn: '2026-08-25', status: 'COMPLETED', totalPrice: 300 }
    ]
    render(<RevenueChart reservations={list} year={2026} />)
    // Total = 200 + 300 = 500 BAM
    expect(screen.getByText(/500.00 BAM/)).toBeInTheDocument()
    // Count = 2 rezervacije
    expect(screen.getByText('2')).toBeInTheDocument()
  })

  it('filtrira rezervacije po godini', () => {
    const list = [
      { checkIn: '2025-08-01', status: 'CONFIRMED', totalPrice: 100 },
      { checkIn: '2026-08-01', status: 'CONFIRMED', totalPrice: 250 }
    ]
    render(<RevenueChart reservations={list} year={2026} />)
    expect(screen.getByText(/250.00 BAM/)).toBeInTheDocument()
  })

  it('renderuje 12 mjeseci', () => {
    const { container } = render(<RevenueChart reservations={[]} year={2026} />)
    expect(container.querySelectorAll('.chart-bar-col').length).toBe(12)
  })

  it('mjesec sa rezervacijama prikazuje broj "X rez"', () => {
    const list = [
      { checkIn: '2026-08-01', status: 'CONFIRMED', totalPrice: 100 },
      { checkIn: '2026-08-15', status: 'CONFIRMED', totalPrice: 200 }
    ]
    render(<RevenueChart reservations={list} year={2026} />)
    expect(screen.getByText('2 rez')).toBeInTheDocument()
  })
})
