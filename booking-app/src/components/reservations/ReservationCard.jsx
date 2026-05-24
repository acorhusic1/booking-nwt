import { useState } from 'react'
import { reservationApi } from '../../api/reservationApi'
import { paymentApi } from '../../api/paymentApi'
import ConfirmModal from '../common/ConfirmModal'
import Spinner from '../common/Spinner'
import { useToast } from '../common/ToastProvider'

/**
 * Reservation kartica sa expand-on-click ponašanjem.
 *
 * SPA princip: klik na kartice ne refreshuje stranicu — samo dohvati
 * payment podatke u pozadini i prikaže ih ispod. Cancel button šalje
 * PUT /api/reservations/{id}/cancel i ažurira state lokalno.
 */
export default function ReservationCard({ reservation, onChanged }) {
  const { showToast } = useToast()
  const [expanded, setExpanded] = useState(false)
  const [payments, setPayments] = useState(null)
  const [loading, setLoading] = useState(false)
  const [cancelling, setCancelling] = useState(false)
  const [error, setError] = useState(null)
  const [confirmOpen, setConfirmOpen] = useState(false)

  const statusClass = (() => {
    const s = (reservation.status || '').toUpperCase()
    if (s === 'CONFIRMED' || s === 'COMPLETED' || s === 'ACTIVE') return 'status status-ok'
    if (s === 'CANCELLED') return 'status status-bad'
    return 'status status-pending'
  })()

  const canCancel = !['CANCELLED', 'COMPLETED'].includes((reservation.status || '').toUpperCase())

  const toggle = async () => {
    const next = !expanded
    setExpanded(next)
    if (next && payments === null) {
      setLoading(true)
      try {
        const data = await paymentApi.getByReservationId(reservation.id)
        setPayments(Array.isArray(data) ? data : [])
      } catch {
        setPayments([])
      } finally {
        setLoading(false)
      }
    }
  }

  const openCancelDialog = (e) => {
    e.stopPropagation()
    setError(null)
    setConfirmOpen(true)
  }

  const performCancel = async () => {
    setCancelling(true)
    try {
      await reservationApi.cancel(reservation.id)
      setConfirmOpen(false)
      showToast({ type: 'success', title: 'Rezervacija otkazana',
        message: `Rezervacija #${reservation.id} je otkazana. Iznos će biti refundiran u nekoliko sekundi.` })
      onChanged?.()
    } catch (err) {
      const msg = err.response?.data?.message
      const text = typeof msg === 'string'
        ? msg
        : 'Otkazivanje nije uspjelo (možda je već CANCELLED ili je prošao rok).'
      setError(text)
      setConfirmOpen(false)
      // Toast je najbolje vidljiv — inline error u kartici je previse sakriven
      showToast({ type: 'error', title: 'Otkazivanje blokirano', message: text, duration: 7000 })
    } finally {
      setCancelling(false)
    }
  }

  return (
    <div className="reservation-card" onClick={toggle}>
      <div className="reservation-header">
        <h3>Rezervacija #{reservation.id}</h3>
        <span className={statusClass}>{reservation.status || 'CREATED'}</span>
      </div>
      <p>Smještaj ID: {reservation.propertyId}</p>
      <p>Dolazak: {reservation.checkIn}</p>
      <p>Odlazak: {reservation.checkOut}</p>
      <p>Osobe: {reservation.numGuests}</p>
      <p className="price">{Number(reservation.totalPrice).toFixed(2)} BAM</p>

      {expanded && (
        <div className="reservation-expand" onClick={(e) => e.stopPropagation()}>
          <h4>💳 Plaćanja</h4>
          {loading && <Spinner size="sm" inline label="Učitavanje plaćanja..." />}
          {!loading && payments?.length === 0 && (
            <p className="loading-inline">Nema upisanih plaćanja za ovu rezervaciju.</p>
          )}
          {!loading && payments?.map(p => (
            <div key={p.id} className="payment-row">
              <span className={`pay-status pay-${(p.status || '').toLowerCase()}`}>{p.status}</span>
              <span>{Number(p.amount).toFixed(2)} {p.currency}</span>
              <span className="pay-method">{p.method}</span>
            </div>
          ))}

          {error && <div className="card-error">{error}</div>}

          {canCancel && (
            <button onClick={openCancelDialog} disabled={cancelling} className="cancel-btn">
              {cancelling ? 'Otkazujem...' : '✕ Otkaži rezervaciju'}
            </button>
          )}
        </div>
      )}

      <ConfirmModal
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={performCancel}
        title="Otkazivanje rezervacije"
        message={`Otkazati rezervaciju #${reservation.id}?`}
        detail="Iznos će biti refundiran na wallet (ako je rezervacija već plaćena)."
        confirmText="Da, otkaži"
        cancelText="Odustani"
        danger
      />
    </div>
  )
}
