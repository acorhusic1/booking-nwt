import { useState, useEffect } from 'react'
import { propertyApi } from '../../api/propertyApi'
import { reservationApi } from '../../api/reservationApi'
import '../../styles/Calendar.css'

/**
 * BUG 8 — Vizualni kalendar za gosta. Klik prvi datum → start, klik drugi → end.
 * Crveno prikazano: postojeci CalendarBlock-ovi (host blokirao) ILI termini sa
 * aktivnim rezervacijama. Gost ne moze izabrati ovakve datume.
 *
 * Kada gost odabere validan raspon, pozove se `onRangeSelected(checkIn, checkOut)`.
 */
const MONTH_NAMES = ['Januar', 'Februar', 'Mart', 'April', 'Maj', 'Juni',
                     'Juli', 'August', 'Septembar', 'Oktobar', 'Novembar', 'Decembar']
const DAY_NAMES = ['P', 'U', 'S', 'Č', 'P', 'S', 'N']

export default function GuestDatePicker({ propertyId, hostId, value, onChange }) {
  const [blocks, setBlocks] = useState([])
  const [reservedRanges, setReservedRanges] = useState([])
  const [viewMonth, setViewMonth] = useState(new Date().getMonth())
  const [viewYear, setViewYear] = useState(new Date().getFullYear())
  const [rangeStart, setRangeStart] = useState(value?.checkIn ? new Date(value.checkIn) : null)
  const [rangeEnd, setRangeEnd] = useState(value?.checkOut ? new Date(value.checkOut) : null)

  // Učitaj blokirane datume + postojeće rezervacije za property
  // (getOccupiedDates je public — vraća samo datume bez sensitive info)
  useEffect(() => {
    if (!propertyId) return
    propertyApi.getCalendarBlocks(propertyId).then(setBlocks).catch(() => setBlocks([]))
    reservationApi.getOccupiedDates(propertyId)
      .then(dates => {
        setReservedRanges((dates || []).map(d => ({
          start: new Date(d.checkIn), end: new Date(d.checkOut)
        })))
      })
      .catch(() => setReservedRanges([]))
  }, [propertyId])

  const buildMonthGrid = () => {
    const firstDay = new Date(viewYear, viewMonth, 1)
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
    if (blocks.find(b => iso >= b.startDate && iso <= b.endDate)) return true
    // Provjeri preklapanje sa drugim rezervacijama (half-open: end day je free)
    return reservedRanges.some(r => d >= r.start && d < r.end)
  }
  const isInSelectedRange = (d) => {
    if (!rangeStart) return false
    if (!rangeEnd) return isoDate(d) === isoDate(rangeStart)
    const iso = isoDate(d)
    const a = isoDate(rangeStart) < isoDate(rangeEnd) ? isoDate(rangeStart) : isoDate(rangeEnd)
    const b = isoDate(rangeStart) < isoDate(rangeEnd) ? isoDate(rangeEnd) : isoDate(rangeStart)
    return iso >= a && iso <= b
  }
  // Provjeri ima li bilo koji blokiran datum izmedju start i end
  const rangeContainsBlocked = (start, end) => {
    const a = start < end ? start : end
    const b = start < end ? end : start
    const cursor = new Date(a)
    while (cursor <= b) {
      if (isBlocked(cursor)) return true
      cursor.setDate(cursor.getDate() + 1)
    }
    return false
  }

  const handleDayClick = (d) => {
    if (!isInMonth(d) || isPast(d) || isBlocked(d)) return
    if (!rangeStart || (rangeStart && rangeEnd)) {
      setRangeStart(d); setRangeEnd(null)
      onChange?.({ checkIn: isoDate(d), checkOut: '' })
    } else {
      const start = d < rangeStart ? d : rangeStart
      const end = d < rangeStart ? rangeStart : d
      if (rangeContainsBlocked(start, end)) {
        // ne dozvoli — raspon sadrzi blokiran datum
        return
      }
      setRangeStart(start); setRangeEnd(end)
      onChange?.({ checkIn: isoDate(start), checkOut: isoDate(end) })
    }
  }

  const navMonth = (delta) => {
    let m = viewMonth + delta
    let y = viewYear
    if (m < 0) { m = 11; y -= 1 }
    else if (m > 11) { m = 0; y += 1 }
    setViewMonth(m); setViewYear(y)
  }

  return (
    <div className="guest-date-picker">
      <div className="calendar-nav">
        <button type="button" onClick={() => navMonth(-1)}>‹</button>
        <strong>{MONTH_NAMES[viewMonth]} {viewYear}</strong>
        <button type="button" onClick={() => navMonth(1)}>›</button>
      </div>

      <div className="calendar-grid">
        {DAY_NAMES.map(d => <div key={d} className="cal-dayname">{d}</div>)}
        {days.map((d) => {
          const inMonth = isInMonth(d)
          const past = isPast(d)
          const blocked = isBlocked(d)
          const selected = isInSelectedRange(d)
          const classes = ['cal-day']
          if (!inMonth) classes.push('out')
          if (past) classes.push('past')
          if (blocked) classes.push('blocked')
          if (selected) classes.push('selected')
          return (
            <div
              key={d.toISOString()}
              className={classes.join(' ')}
              onClick={() => handleDayClick(d)}
              title={blocked ? 'Zauzeto' : past ? 'Datum je prošao' : ''}
            >
              {d.getDate()}
            </div>
          )
        })}
      </div>

      <div className="guest-date-picker-legend">
        <span><span className="legend-box past"></span> Prošlo</span>
        <span><span className="legend-box blocked"></span> Zauzeto / blokirano</span>
        <span><span className="legend-box selected"></span> Vaš odabir</span>
      </div>

      {rangeStart && (
        <div className="guest-date-picker-summary">
          <strong>Odabrano:</strong> {isoDate(rangeStart)}
          {rangeEnd ? ` → ${isoDate(rangeEnd)}` : ' (klikni drugi datum za check-out)'}
        </div>
      )}
    </div>
  )
}
