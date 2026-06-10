import { useState, useEffect, useMemo } from 'react'
import { reviewApi } from '../../api/reviewApi'
import StarRating from '../common/StarRating'

/**
 * F11 expansion — Host analytics panel:
 *   - Filter po objektu (dropdown)
 *   - Stopa popunjenosti (occupancy %) za izabranu godinu
 *   - Prosjecna ocjena po objektu + ukupna
 *   - Najpopularniji smjestaji (rank po broju rezervacija)
 *   - Bar chart prihoda po mjesecu (passthrough kroz children za nezavisno pozicioniranje)
 */
export default function HostAnalyticsPanel({ properties, reservations, year, selectedPropertyId, onSelectProperty }) {
  const [reviewsByProp, setReviewsByProp] = useState({})

  // Ucitaj recenzije za svaki property jednom
  useEffect(() => {
    let cancelled = false
    Promise.all(
      properties.map(p =>
        reviewApi.getByProperty(p.id).then(rs => [p.id, rs || []]).catch(() => [p.id, []])
      )
    ).then(pairs => {
      if (cancelled) return
      const map = {}
      pairs.forEach(([id, rs]) => { map[id] = rs })
      setReviewsByProp(map)
    })
    return () => { cancelled = true }
  }, [properties.map(p => p.id).join(',')])

  // Filtriraj rezervacije za izabranu godinu (+ opcionalno property)
  const relevantReservations = useMemo(() => {
    return reservations.filter(r => {
      if (!['CONFIRMED', 'ACTIVE', 'COMPLETED'].includes((r.status || '').toUpperCase())) return false
      if (!r.checkIn) return false
      if (new Date(r.checkIn).getFullYear() !== year) return false
      if (selectedPropertyId && r.propertyId !== selectedPropertyId) return false
      return true
    })
  }, [reservations, year, selectedPropertyId])

  // Stopa popunjenosti — booked nights / available nights u godini
  // Available = 365 (366 za prijestupnu) × broj relevantnih propertya
  const occupancy = useMemo(() => {
    // Noci se SIJEKU na granice izabrane godine — boravak koji prelazi u
    // sljedecu godinu ranije je brojan cijeli pa je procenat bio naduvan.
    const yearStart = new Date(year, 0, 1)
    const yearEnd = new Date(year + 1, 0, 1)
    const totalNights = relevantReservations.reduce((sum, r) => {
      if (!r.checkIn || !r.checkOut) return sum
      const from = new Date(Math.max(new Date(r.checkIn), yearStart))
      const to = new Date(Math.min(new Date(r.checkOut), yearEnd))
      const nights = Math.max(0, Math.round((to - from) / 86400000))
      return sum + nights
    }, 0)
    const daysInYear = ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0) ? 366 : 365
    const propsCount = selectedPropertyId
      ? 1
      : properties.length
    const availableNights = daysInYear * Math.max(1, propsCount)
    return availableNights > 0 ? Math.min(100, (totalNights / availableNights) * 100) : 0
  }, [relevantReservations, year, properties.length, selectedPropertyId])

  // Avg ocjena
  const ratingsByProp = useMemo(() => {
    const out = {}
    Object.entries(reviewsByProp).forEach(([pid, rs]) => {
      if (!rs.length) return
      const avg = rs.reduce((s, r) => s + Number(r.overallRating || 0), 0) / rs.length
      out[pid] = { avg, count: rs.length }
    })
    return out
  }, [reviewsByProp])

  const overallAvgRating = useMemo(() => {
    const all = Object.values(ratingsByProp)
    if (!all.length) return 0
    const totalCount = all.reduce((s, x) => s + x.count, 0)
    if (!totalCount) return 0
    const weighted = all.reduce((s, x) => s + x.avg * x.count, 0)
    return weighted / totalCount
  }, [ratingsByProp])

  // Najpopularniji smjestaji — rank po broju rezervacija (u izabranoj godini)
  const popularRanking = useMemo(() => {
    const counts = {}
    relevantReservations.forEach(r => {
      counts[r.propertyId] = (counts[r.propertyId] || 0) + 1
    })
    return properties
      .map(p => ({
        ...p,
        bookings: counts[p.id] || 0,
        avgRating: ratingsByProp[p.id]?.avg || 0,
        reviewCount: ratingsByProp[p.id]?.count || 0
      }))
      .sort((a, b) => b.bookings - a.bookings)
  }, [relevantReservations, properties, ratingsByProp])

  return (
    <div className="host-analytics-panel">
      <div className="analytics-controls glass-panel"
           style={{ padding: '15px', marginBottom: '20px', display: 'flex', gap: '15px',
                    alignItems: 'center', flexWrap: 'wrap' }}>
        <strong>🎯 Filter:</strong>
        <label style={{ display: 'flex', alignItems: 'center', gap: '6px',
                        color: 'var(--text-tertiary)', fontSize: '0.85rem' }}>
          Smještaj:
          <select value={selectedPropertyId || ''}
                  onChange={e => onSelectProperty(e.target.value ? Number(e.target.value) : null)}
                  className="admin-select">
            <option value="">Svi smještaji</option>
            {properties.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="host-stats" style={{ marginBottom: '20px' }}>
        <div className="stat-card glass-panel">
          <span className="stat-icon">📊</span>
          <div className="stat-info">
            <span className="stat-value">{occupancy.toFixed(1)}%</span>
            <span className="stat-label">Popunjenost ({year})</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">⭐</span>
          <div className="stat-info">
            <span className="stat-value">{overallAvgRating.toFixed(1)}</span>
            <span className="stat-label">Prosječna ocjena</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">🏆</span>
          <div className="stat-info">
            <span className="stat-value">
              {popularRanking[0]?.bookings > 0 ? popularRanking[0]?.name?.slice(0, 14) + (popularRanking[0]?.name?.length > 14 ? '…' : '') : '—'}
            </span>
            <span className="stat-label">Najpopularniji</span>
          </div>
        </div>
      </div>

      <section className="host-section glass-panel" style={{ marginBottom: '20px' }}>
        <h3 style={{ marginTop: 0 }}>🏆 Rang lista smještaja ({year})</h3>
        {popularRanking.length === 0 ? (
          <p className="no-data">Nema podataka.</p>
        ) : (
          <table className="reservations-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Smještaj</th>
                <th>Rezervacija ({year})</th>
                <th>Recenzija</th>
                <th>Ocjena</th>
              </tr>
            </thead>
            <tbody>
              {popularRanking.map((p, idx) => (
                <tr key={p.id}>
                  <td>{idx + 1}.</td>
                  <td>{p.name}</td>
                  <td>{p.bookings}</td>
                  <td>{p.reviewCount}</td>
                  <td>
                    {p.reviewCount > 0 ? (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                        <StarRating value={p.avgRating} readOnly size="sm" />
                        <em>({p.avgRating.toFixed(1)})</em>
                      </span>
                    ) : <em>nema</em>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  )
}
