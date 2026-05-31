import { describe, it, expect } from 'vitest'
import { calculateReservationPrice } from '../../utils/pricing'

describe('calculateReservationPrice', () => {
  const flat = { basePrice: 100, weekendPrice: 100, longStayThreshold: 0, longStayDiscountPct: 0 }

  it('vraca prazan rezultat kad nedostaju datumi', () => {
    expect(calculateReservationPrice(flat, '', '').nights).toBe(0)
    expect(calculateReservationPrice(null, '2026-08-01', '2026-08-03').nights).toBe(0)
  })

  it('racuna 3 noci flat rate', () => {
    const r = calculateReservationPrice(flat, '2026-08-03', '2026-08-06')
    expect(r.nights).toBe(3)
    expect(r.subtotal).toBe(300)
    expect(r.total).toBe(300)
  })

  it('razlikuje vikend cijenu', () => {
    const p = { basePrice: 100, weekendPrice: 150, longStayThreshold: 0, longStayDiscountPct: 0 }
    // 2026-08-08 Sub, 09 Ned, 10 Pon → 2 vikend + 1 radni
    const r = calculateReservationPrice(p, '2026-08-08', '2026-08-11')
    expect(r.nights).toBe(3)
    expect(r.subtotal).toBe(150 + 150 + 100)
  })

  it('primjenjuje long-stay popust na subtotal', () => {
    const p = { basePrice: 100, weekendPrice: 100, longStayThreshold: 7, longStayDiscountPct: 10 }
    const r = calculateReservationPrice(p, '2026-08-01', '2026-08-08')
    expect(r.nights).toBe(7)
    expect(r.subtotal).toBe(700)
    expect(r.longStayDiscount).toBe(70)
    expect(r.total).toBe(630)
  })

  it('primjenjuje sezonsku korekciju (+30%)', () => {
    const seasonal = [{ name: 'Ljeto', startDate: '2026-07-01', endDate: '2026-08-31', priceModifierPct: 30 }]
    const r = calculateReservationPrice(flat, '2026-08-01', '2026-08-03', seasonal)
    expect(r.nights).toBe(2)
    expect(r.activeSeasons).toContain('Ljeto')
    expect(r.seasonalAdjustment).toBe(60)
    expect(r.subtotal).toBe(260)
  })

  it('detektuje min-nights violation iz sezone', () => {
    const seasonal = [{ name: 'NG', startDate: '2026-12-28', endDate: '2027-01-05', priceModifierPct: 0, minNights: 5 }]
    const r = calculateReservationPrice(flat, '2026-12-30', '2027-01-01', seasonal)
    expect(r.minNightsViolation).toEqual({ season: 'NG', required: 5, actual: 2 })
  })

  it('total nikad nije negativan', () => {
    const p = { basePrice: 100, weekendPrice: 100, longStayThreshold: 1, longStayDiscountPct: 200 }
    const r = calculateReservationPrice(p, '2026-08-01', '2026-08-03')
    expect(r.total).toBe(0)
  })
})
