import { useState, useEffect } from 'react'
import Modal from '../common/Modal'
import { propertyApi } from '../../api/propertyApi'
import { reservationApi } from '../../api/reservationApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import '../../styles/Calendar.css'

/**
 * F3 — Vizualni kalendar dostupnosti za hosta.
 * Klik na datum → start selekcije. Klik na drugi → end. "Blokiraj" snima blok.
 * Postojeci blokovi su prikazani crveno, mogu se obrisati.
 */
const MONTH_NAMES = ['Januar', 'Februar', 'Mart', 'April', 'Maj', 'Juni',
                     'Juli', 'August', 'Septembar', 'Oktobar', 'Novembar', 'Decembar']
const DAY_NAMES = ['P', 'U', 'S', 'Č', 'P', 'S', 'N']

export default function CalendarModal({ open, onClose, property }) {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const [blocks, setBlocks] = useState([])
  // BUG H — host treba vidjeti i gostove rezervacije da ne stavi radove preko njih
  const [reservedRanges, setReservedRanges] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [viewMonth, setViewMonth] = useState(new Date().getMonth())
  const [viewYear, setViewYear] = useState(new Date().getFullYear())
  const [rangeStart, setRangeStart] = useState(null)
  const [rangeEnd, setRangeEnd] = useState(null)
  const [reason, setReason] = useState('')

  const load = async () => {
    if (!property?.id) return
    setLoading(true)
    try {
      const [blocksData, occupied] = await Promise.allSettled([
        propertyApi.getCalendarBlocks(property.id),
        reservationApi.getOccupiedDates(property.id)
      ])
      setBlocks(blocksData.status === 'fulfilled' ? (blocksData.value || []) : [])
      setReservedRanges(occupied.status === 'fulfilled' ? (occupied.value || []) : [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [property?.id])

  // Build matrix: array of dates for current month grid (6 weeks × 7 days)
  const buildMonthGrid = () => {
    const firstDay = new Date(viewYear, viewMonth, 1)
    // Mon=0 ... Sun=6 (EU layout)
    const offset = (firstDay.getDay() + 6) % 7
    const gridStart = new Date(viewYear, viewMonth, 1 - offset)
    const days = []
    for (let i = 0; i < 42; i++) {
      const d = new Date(gridStart)
      d.setDate(gridStart.getDate() + i)
      days.push(d)
    }
    return days
  }

  const days = buildMonthGrid()
  const today = new Date(new Date().toDateString())

  const isoDate = (d) => d.toISOString().split('T')[0]
  const isInMonth = (d) => d.getMonth() === viewMonth
  const isPast = (d) => d < today
  const isBlocked = (d) => {
    const iso = isoDate(d)
    return blocks.find(b => iso >= b.startDate && iso <= b.endDate)
  }
  // checkOut je exclusive — gost odlazi tog dana pa je dan slobodan
  const isReserved = (d) => {
    const iso = isoDate(d)
    return reservedRanges.find(r => {
      const s = (r.checkIn || '').slice(0, 10)
      const e = (r.checkOut || '').slice(0, 10)
      return s && e && iso >= s && iso < e
    })
  }
  const isInSelectedRange = (d) => {
    if (!rangeStart) return false
    if (!rangeEnd) return isoDate(d) === isoDate(rangeStart)
    const iso = isoDate(d)
    const a = isoDate(rangeStart) < isoDate(rangeEnd) ? isoDate(rangeStart) : isoDate(rangeEnd)
    const b = isoDate(rangeStart) < isoDate(rangeEnd) ? isoDate(rangeEnd) : isoDate(rangeStart)
    return iso >= a && iso <= b
  }

  const handleDayClick = (d) => {
    if (!isInMonth(d) || isPast(d) || isBlocked(d) || isReserved(d)) return
    if (!rangeStart || (rangeStart && rangeEnd)) {
      setRangeStart(d); setRangeEnd(null)
    } else {
      // postaviti end (poredjamo redoslijed)
      if (d < rangeStart) {
        setRangeEnd(rangeStart); setRangeStart(d)
      } else {
        setRangeEnd(d)
      }
    }
  }

  const navMonth = (delta) => {
    let m = viewMonth + delta
    let y = viewYear
    if (m < 0) { m = 11; y -= 1 }
    else if (m > 11) { m = 0; y += 1 }
    setViewMonth(m); setViewYear(y)
  }

  const handleSaveBlock = async () => {
    if (!rangeStart) return
    const start = isoDate(rangeStart)
    const end = isoDate(rangeEnd || rangeStart)
    setSaving(true)
    try {
      await propertyApi.addCalendarBlock(property.id, {
        startDate: start, endDate: end, reason: reason.trim() || 'Nedostupno', createdBy: user.id
      })
      showToast({ type: 'success', title: 'Blokirano', message: `Datumi ${start} → ${end} su blokirani.` })
      setRangeStart(null); setRangeEnd(null); setReason('')
      load()
    } catch (err) {
      showToast({ type: 'error', title: 'Greška', message: err.response?.data?.message || 'Blok nije sačuvan.' })
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteBlock = async (blockId) => {
    try {
      await propertyApi.deleteCalendarBlock(property.id, blockId)
      showToast({ type: 'success', title: 'Obrisano', message: 'Blok je uklonjen — datumi su opet dostupni.' })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Blok nije obrisan.' })
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={`Kalendar: ${property?.name || ''}`} size="lg">
      <div className="calendar-nav">
        <button type="button" onClick={() => navMonth(-1)}>‹</button>
        <strong>{MONTH_NAMES[viewMonth]} {viewYear}</strong>
        <button type="button" onClick={() => navMonth(1)}>›</button>
      </div>

      <div className="calendar-grid">
        {DAY_NAMES.map(d => <div key={d} className="cal-dayname">{d}</div>)}
        {days.map((d) => {
          const block = isBlocked(d)
          const reserved = !block && isReserved(d)
          const inMonth = isInMonth(d)
          const past = isPast(d)
          const selected = isInSelectedRange(d)
          const classes = ['cal-day']
          if (!inMonth) classes.push('out')
          if (past) classes.push('past')
          if (block) classes.push('blocked')
          else if (reserved) classes.push('reserved')
          if (selected) classes.push('selected')
          const title = block ? `Blokirano: ${block.reason}`
            : reserved ? 'Rezervisano (gost)' : ''
          return (
            <div
              key={d.toISOString()}
              className={classes.join(' ')}
              onClick={() => handleDayClick(d)}
              title={title}
            >
              {d.getDate()}
            </div>
          )
        })}
      </div>

      {rangeStart && (
        <div className="calendar-selection">
          <div>
            Odabran period: <strong>{isoDate(rangeStart)} {rangeEnd ? `→ ${isoDate(rangeEnd)}` : '(klikni drugi datum)'}</strong>
          </div>
          <input
            type="text"
            placeholder="Razlog (opciono, npr. Renoviranje)"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            disabled={saving}
          />
          <div style={{ display: 'flex', gap: '8px' }}>
            <button type="button" className="btn-secondary" onClick={() => { setRangeStart(null); setRangeEnd(null) }}>Otkaži</button>
            <button type="button" className="btn-primary" onClick={handleSaveBlock} disabled={saving}>
              {saving ? 'Spremam...' : '🚫 Blokiraj period'}
            </button>
          </div>
        </div>
      )}

      <div className="calendar-blocks-list">
        <h4>Postojeći blokovi</h4>
        {loading ? (
          <p className="modal-hint">Učitavanje...</p>
        ) : blocks.length === 0 ? (
          <p className="modal-hint">Nema blokiranih perioda — svi datumi su dostupni.</p>
        ) : (
          blocks.map(b => (
            <div key={b.id} className="cal-block-item">
              <span>
                <strong>{b.startDate} → {b.endDate}</strong>
                <em> {b.reason}</em>
              </span>
              <button onClick={() => handleDeleteBlock(b.id)}>🗑</button>
            </div>
          ))
        )}
      </div>
    </Modal>
  )
}
