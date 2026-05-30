import { useMemo } from 'react'
import '../../styles/RevenueChart.css'

/**
 * BUG 9 — Bar chart zarade po mjesecu (CSS-only, bez external lib).
 * Računa zaradu iz CONFIRMED + ACTIVE + COMPLETED rezervacija za izabranu godinu.
 */
const MONTH_SHORT = ['Jan', 'Feb', 'Mar', 'Apr', 'Maj', 'Jun',
                     'Jul', 'Avg', 'Sep', 'Okt', 'Nov', 'Dec']

export default function RevenueChart({ reservations, year }) {
  const monthData = useMemo(() => {
    const counts = Array(12).fill(0)
    const revenue = Array(12).fill(0)

    for (const r of reservations || []) {
      const status = (r.status || '').toUpperCase()
      // Pribrojavamo samo realizovane (ne CREATED jer Saga još uvijek treba potvrditi, ne CANCELLED)
      if (!['CONFIRMED', 'ACTIVE', 'COMPLETED'].includes(status)) continue
      if (!r.checkIn) continue
      const d = new Date(r.checkIn)
      if (d.getFullYear() !== year) continue
      const m = d.getMonth()
      counts[m] += 1
      revenue[m] += Number(r.totalPrice) || 0
    }
    return { counts, revenue }
  }, [reservations, year])

  const maxRevenue = Math.max(1, ...monthData.revenue)
  const totalRevenue = monthData.revenue.reduce((a, b) => a + b, 0)
  const totalReservations = monthData.counts.reduce((a, b) => a + b, 0)

  return (
    <div className="revenue-chart">
      <div className="chart-summary">
        <div>
          <span className="chart-summary-label">Ukupno rezervacija ({year})</span>
          <span className="chart-summary-value">{totalReservations}</span>
        </div>
        <div>
          <span className="chart-summary-label">Ukupna zarada ({year})</span>
          <span className="chart-summary-value">{totalRevenue.toFixed(2)} BAM</span>
        </div>
      </div>

      <div className="chart-bars" role="img" aria-label="Bar chart zarade po mjesecu">
        {monthData.revenue.map((rev, idx) => {
          const heightPct = maxRevenue > 0 ? (rev / maxRevenue) * 100 : 0
          return (
            <div key={idx} className="chart-bar-col">
              <div className="chart-bar-wrapper">
                <div className="chart-bar-value">{rev > 0 ? rev.toFixed(0) : ''}</div>
                <div
                  className="chart-bar"
                  style={{ height: `${heightPct}%` }}
                  title={`${MONTH_SHORT[idx]} ${year}: ${rev.toFixed(2)} BAM (${monthData.counts[idx]} rez.)`}
                />
              </div>
              <div className="chart-bar-label">{MONTH_SHORT[idx]}</div>
              {monthData.counts[idx] > 0 && (
                <div className="chart-bar-count">{monthData.counts[idx]} rez</div>
              )}
            </div>
          )
        })}
      </div>

      {totalReservations === 0 && (
        <p className="chart-empty">Nema rezervacija za izabranu godinu.</p>
      )}
    </div>
  )
}
