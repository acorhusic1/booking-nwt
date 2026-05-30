import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { reservationApi } from '../../api/reservationApi'
import { paymentApi } from '../../api/paymentApi'
import { messagesApi } from '../../api/messagesApi'
import ConfirmModal from '../common/ConfirmModal'
import ReviewModal from './ReviewModal'
import ProblemReportModal from './ProblemReportModal'
import Spinner from '../common/Spinner'
import { useToast } from '../common/ToastProvider'
import { useAuthStore } from '../../store/authStore'

/**
 * Reservation kartica sa expand-on-click ponašanjem.
 *
 * SPA princip: klik na kartice ne refreshuje stranicu — samo dohvati
 * payment podatke u pozadini i prikaže ih ispod. Cancel button šalje
 * PUT /api/reservations/{id}/cancel i ažurira state lokalno.
 */
export default function ReservationCard({ reservation, onChanged }) {
  const { showToast } = useToast()
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const [contactingHost, setContactingHost] = useState(false)
  const [expanded, setExpanded] = useState(false)
  const [payments, setPayments] = useState(null)
  const [loading, setLoading] = useState(false)
  const [cancelling, setCancelling] = useState(false)
  const [error, setError] = useState(null)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [reviewOpen, setReviewOpen] = useState(false)
  const [reportOpen, setReportOpen] = useState(false)

  const statusClass = (() => {
    const s = (reservation.status || '').toUpperCase()
    if (s === 'CONFIRMED' || s === 'COMPLETED' || s === 'ACTIVE') return 'status status-ok'
    if (s === 'CANCELLED') return 'status status-bad'
    return 'status status-pending'
  })()

  const canCancel = !['CANCELLED', 'COMPLETED'].includes((reservation.status || '').toUpperCase())

  // BUG 7 — gost moze otvoriti konverzaciju sa hostom direktno iz kartice
  const messageHost = async (e) => {
    e.stopPropagation()
    if ((user?.role || '').toUpperCase() !== 'GUEST') return
    setContactingHost(true)
    try {
      await messagesApi.createConversation(reservation.guestId, reservation.hostId, reservation.propertyId, reservation.id)
      showToast({ type: 'success', title: 'Konverzacija otvorena', message: 'Preusmjeravam vas na poruke...' })
      setTimeout(() => navigate('/messages'), 600)
    } catch (err) {
      showToast({ type: 'error', title: 'Greška', message: err.response?.data?.message || 'Nije moguće otvoriti konverzaciju.' })
    } finally {
      setContactingHost(false)
    }
  }
  // F7 — recenzija nakon zavrsenog boravka (ili kad je checkout prosao a scheduler jos nije stigao)
  const status = (reservation.status || '').toUpperCase()
  const checkoutPassed = reservation.checkOut && new Date(reservation.checkOut) < new Date(new Date().toDateString())
  const canReview = status === 'COMPLETED' || ((status === 'CONFIRMED' || status === 'ACTIVE') && checkoutPassed)
  // F17 — problem se moze prijaviti tokom aktivnog/potvrdjenog boravka
  const canReport = ['CONFIRMED', 'ACTIVE'].includes(status) && !checkoutPassed

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

          {canReview && (
            <button onClick={(e) => { e.stopPropagation(); setReviewOpen(true) }} className="review-btn">
              ⭐ Ostavi recenziju
            </button>
          )}

          {canReport && (
            <button onClick={(e) => { e.stopPropagation(); setReportOpen(true) }} className="report-btn">
              ⚠ Prijavi problem
            </button>
          )}

          {(user?.role || '').toUpperCase() === 'GUEST' && (
            <button onClick={messageHost} disabled={contactingHost} className="message-host-btn">
              {contactingHost ? 'Otvaram...' : '💬 Pošalji poruku domaćinu'}
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

      {reviewOpen && (
        <ReviewModal
          open={reviewOpen}
          onClose={() => setReviewOpen(false)}
          reservation={reservation}
          onSubmitted={onChanged}
        />
      )}

      {reportOpen && (
        <ProblemReportModal
          open={reportOpen}
          onClose={() => setReportOpen(false)}
          reservation={reservation}
          reporterId={user?.id}
          onSubmitted={onChanged}
        />
      )}
    </div>
  )
}
