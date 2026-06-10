/**
 * F4 + F15 — Smart price calculator sa sezonskim pravilima.
 *
 * Pravila iz PricingRule (per-property):
 *   - basePrice            — cijena po noći (radni dan)
 *   - weekendPrice         — cijena po noći (Sub/Ned), opciono
 *   - longStayThreshold    — broj noćenja od kojeg krece long-stay popust
 *   - longStayDiscountPct  — % popusta za long stay
 *
 * Sezonska pravila (lista):
 *   - startDate, endDate   — period vazenja
 *   - priceModifierPct     — % korekcije cijene (npr. +30 = +30%, -10 = -10%)
 *   - minNights            — minimum boravka u periodu
 *
 * Vraca: { perNightBreakdown, subtotal, longStayDiscount, total, nights, seasonalAdjustment, activeSeasons, minNightsViolation, stayViolation }
 */
export function calculateReservationPrice(pricing, checkIn, checkOut, seasonalRules = []) {
  const empty = {
    perNightBreakdown: [], subtotal: 0, longStayDiscount: 0, total: 0, nights: 0,
    seasonalAdjustment: 0, activeSeasons: [], minNightsViolation: null, stayViolation: null
  }
  if (!pricing || !checkIn || !checkOut) return empty

  const start = new Date(checkIn)
  const end = new Date(checkOut)
  const nights = Math.round((end - start) / (1000 * 60 * 60 * 24))
  if (nights <= 0) return empty

  // F3 — min/max nocenja iz cjenovnika (backend ovo isto provodi pa formu
  // treba blokirati prije submita s jasnom porukom umjesto sirove 400 greske)
  let stayViolation = null
  const minStay = Number(pricing.minStayDays) || 0
  const maxStay = Number(pricing.maxStayDays) || 0
  if (minStay > 0 && nights < minStay) {
    stayViolation = { type: 'min', required: minStay, actual: nights }
  } else if (maxStay > 0 && nights > maxStay) {
    stayViolation = { type: 'max', required: maxStay, actual: nights }
  }

  const basePrice = Number(pricing.basePrice) || 0
  const weekendPrice = Number(pricing.weekendPrice) || basePrice

  const perNightBreakdown = []
  let subtotal = 0
  let seasonalAdjustment = 0
  const activeSeasons = new Set()
  let minNightsViolation = null

  // Pre-process sezonska pravila u Date objekte
  const seasons = (seasonalRules || []).map(s => ({
    ...s,
    _start: new Date(s.startDate),
    _end: new Date(s.endDate)
  }))

  for (let i = 0; i < nights; i++) {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    const dayOfWeek = date.getDay()
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
    let nightlyPrice = isWeekend ? weekendPrice : basePrice

    // Primijeni sve aktivne sezonske modifikatore (sume se)
    let nightSeasonalDelta = 0
    for (const s of seasons) {
      if (date >= s._start && date <= s._end) {
        activeSeasons.add(s.name)
        const modifier = (Number(s.priceModifierPct) || 0) / 100
        nightSeasonalDelta += nightlyPrice * modifier
        if (s.minNights && nights < s.minNights && !minNightsViolation) {
          minNightsViolation = { season: s.name, required: s.minNights, actual: nights }
        }
      }
    }
    nightlyPrice += nightSeasonalDelta
    seasonalAdjustment += nightSeasonalDelta

    // lokalni format umjesto toISOString() — UTC konverzija pomjera datum -1 dan
    const localDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    perNightBreakdown.push({ date: localDate, isWeekend, price: nightlyPrice })
    subtotal += nightlyPrice
  }

  // Long stay popust (NAKON sezonske korekcije)
  let longStayDiscount = 0
  const threshold = Number(pricing.longStayThreshold) || 0
  const discountPct = Number(pricing.longStayDiscountPct) || 0
  if (threshold > 0 && nights >= threshold && discountPct > 0) {
    longStayDiscount = Math.round(subtotal * (discountPct / 100) * 100) / 100
  }

  const total = Math.max(0, subtotal - longStayDiscount)

  return {
    perNightBreakdown,
    subtotal,
    longStayDiscount,
    total,
    nights,
    seasonalAdjustment: Math.round(seasonalAdjustment * 100) / 100,
    activeSeasons: Array.from(activeSeasons),
    minNightsViolation,
    stayViolation
  }
}
