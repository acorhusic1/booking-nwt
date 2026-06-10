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

  // F6 — ocekivani refund po ISTOM pravilu kao backend (default politika:
  // >= 7 dana prije dolaska → 100%, inace 0%; CREATED → uvijek pun povrat).
  // Tacna politika objekta moze odstupati — backend je autoritativan.
  const refundInfo = (() => {
    // Ako su placanja vec ucitana (cancel dugme je u expanded prikazu) i NEMA
    // uspjesne naplate, nema ni povrata — npr. rezervacija potvrdjena na starom
    // buildu iako je naplata pala. Bez ovoga poruka lazno obecava refund.
    if (payments !== null && !payments.some(p => (p.status || '').toUpperCase() === 'COMPLETED')) {
      return { pct: 0, text: 'Za ovu rezervaciju nije evidentirana uspješna naplata — nema iznosa za povrat.' }
    }
    const s = (reservation.status || '').toUpperCase()
    if (s === 'CREATED') {
      return { pct: 100, text: 'Plaćanje još nije potvrđeno — iznos se vraća u potpunosti (ako je naplaćen).' }
    }
    if (s !== 'CONFIRMED') return { pct: null, text: '' }
    const daysUntil = reservation.checkIn
      ? Math.ceil((new Date(reservation.checkIn) - new Date(new Date().toDateString())) / 86400000)
      : null
    if (daysUntil == null) return { pct: 100, text: 'Iznos će biti vraćen na novčanik.' }
    if (daysUntil >= 7) {
      return { pct: 100, text: `Besplatno otkazivanje (${daysUntil} dana do dolaska) — puni iznos od ${Number(reservation.totalPrice).toFixed(2)} BAM se vraća na novčanik za par sekundi.` }
    }
    return { pct: 0, text: `Otkazujete ${daysUntil} ${daysUntil === 1 ? 'dan' : 'dana'} prije dolaska (besplatno je do 7 dana) — prema politici otkazivanja POVRAT JE 0%.` }
  })()

  // BUG 7 — gost moze otvoriti konverzaciju sa hostom direktno iz kartice
  const messageHost = async (e) => {
    e.stopPropagation()
    if ((user?.role || '').toUpperCase() !== 'GUEST') return
    setContactingHost(true)
    try {
      const conv = await messagesApi.createConversation(reservation.guestId, reservation.hostId, reservation.propertyId, reservation.id)
      showToast({ type: 'success', title: 'Konverzacija otvorena', message: 'Preusmjeravam vas na poruke...' })
      // BUG F — bez ?conv=X Messages stranica bi otvorila prvu konverzaciju
      // umjesto upravo kreirane.
      setTimeout(() => navigate(`/messages?conv=${conv?.id || ''}`), 600)
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
      showToast({
        type: refundInfo.pct === 0 ? 'warning' : 'success',
        title: 'Rezervacija otkazana',
        message: `Rezervacija #${reservation.id} je otkazana. ${refundInfo.text}`,
        duration: 8000
      })
      onChanged?.()
      // Refund ide asinhrono (RabbitMQ) — osvjezi wallet/listu jos jednom
      // kad kompenzacija sjedne, da korisnik vidi novi balans bez F5.
      setTimeout(() => onChanged?.(), 3000)
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
        detail={refundInfo.text || 'Iznos će biti refundiran na wallet (ako je rezervacija već plaćena).'}
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
