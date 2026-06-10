import { useEffect, useMemo, useState } from 'react'
import { propertyApi } from '../../api/propertyApi'
import PropertyCard from './PropertyCard'

/**
 * F12 — Personalizirane preporuke smještaja na osnovu historije rezervacija.
 *
 * Jednostavna content-based logika (bez ML-a, u skladu sa "spremnost za AI"
 * iz dokumentacije):
 *   1. Iz prošlih rezervacija gosta izvuče preferirane gradove i prosječan
 *      budžet po noći (totalPrice / broj noći).
 *   2. Rangira dostupne smještaje: +2 boda grad iz historije, +1 bod cijena
 *      unutar ±40% prosječnog budžeta.
 *   3. Smještaji koje je gost već rezervisao se izuzimaju.
 *   4. Bez historije → fallback na najnovije dostupne smještaje.
 */
export default function RecommendedProperties({ reservations = [], limit = 4 }) {
  const [allProperties, setAllProperties] = useState([])
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    propertyApi.getAll(0, 1000, '')
      .then(data => setAllProperties(data.content || []))
      .catch(() => setAllProperties([]))
      .finally(() => setLoaded(true))
  }, [])

  const recommended = useMemo(() => {
    const available = allProperties.filter(p => p.available)
    if (available.length === 0) return []

    const past = reservations.filter(r =>
      ['CONFIRMED', 'ACTIVE', 'COMPLETED'].includes((r.status || '').toUpperCase()))
    const reservedPropertyIds = new Set(past.map(r => r.propertyId))

    // Preferencije iz historije
    const cityCount = {}
    let budgetSum = 0
    let budgetN = 0
    past.forEach(r => {
      const prop = allProperties.find(p => p.id === r.propertyId)
      if (prop?.city) cityCount[prop.city] = (cityCount[prop.city] || 0) + 1
      const nights = r.checkIn && r.checkOut
        ? Math.max(1, Math.round((new Date(r.checkOut) - new Date(r.checkIn)) / 86400000))
        : 0
      if (nights > 0 && r.totalPrice) {
        budgetSum += Number(r.totalPrice) / nights
        budgetN++
      }
    })
    const avgBudget = budgetN > 0 ? budgetSum / budgetN : null

    const candidates = available.filter(p => !reservedPropertyIds.has(p.id))

    if (past.length === 0) {
      // Fallback — najnoviji dostupni smještaji
      return candidates
        .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
        .slice(0, limit)
    }

    return candidates
      .map(p => {
        let score = 0
        if (p.city && cityCount[p.city]) score += 2 * cityCount[p.city]
        const price = Number(p.basePrice) || 0
        if (avgBudget != null && price > 0
            && price >= avgBudget * 0.6 && price <= avgBudget * 1.4) {
          score += 1
        }
        return { ...p, _score: score }
      })
      .sort((a, b) => b._score - a._score)
      .slice(0, limit)
  }, [allProperties, reservations, limit])

  if (!loaded || recommended.length === 0) return null

  return (
    <section className="recommended-section">
      <h2>🎯 Preporučeno za vas</h2>
      <p className="recommended-hint">
        Na osnovu vaših prethodnih rezervacija (lokacije i budžet).
      </p>
      <div className="properties-grid">
        {recommended.map(p => (
          <PropertyCard key={p.id} property={p} />
        ))}
      </div>
    </section>
  )
}
