import { useState } from 'react'
import Modal from '../common/Modal'
import StarRating from '../common/StarRating'
import { reviewApi } from '../../api/reviewApi'
import { useToast } from '../common/ToastProvider'

/**
 * Modal za ostavljanje recenzije (F7). Otvara se sa završene rezervacije.
 * 5 kategorija ocjena + tekstualni komentar. Backend računa overallRating.
 */
const CATEGORIES = [
  { key: 'ratingCleanliness', label: 'Čistoća' },
  { key: 'ratingLocation', label: 'Lokacija' },
  { key: 'ratingCommunication', label: 'Komunikacija' },
  { key: 'ratingValue', label: 'Vrijednost za novac' },
  { key: 'ratingAccuracy', label: 'Tačnost opisa' }
]

export default function ReviewModal({ open, onClose, reservation, onSubmitted }) {
  const { showToast } = useToast()
  const [ratings, setRatings] = useState({
    ratingCleanliness: 0, ratingLocation: 0, ratingCommunication: 0,
    ratingValue: 0, ratingAccuracy: 0
  })
  const [comment, setComment] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const setRating = (key, val) => setRatings((r) => ({ ...r, [key]: val }))

  const allRated = CATEGORIES.every((c) => ratings[c.key] > 0)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!allRated) {
      setError('Ocijenite sve kategorije')
      return
    }
    setError(null)
    setBusy(true)
    try {
      await reviewApi.create({
        reservationId: reservation.id,
        guestId: reservation.guestId,
        propertyId: reservation.propertyId,
        hostId: reservation.hostId,
        ...ratings,
        comment: comment.trim()
      })
      showToast({ type: 'success', title: 'Recenzija poslana', message: 'Hvala na ocjeni!' })
      onSubmitted?.()
      onClose()
    } catch (err) {
      const msg = err.response?.data?.message || 'Greška pri slanju recenzije'
      setError(msg)
      showToast({ type: 'error', title: 'Greška', message: msg })
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal open={open} onClose={busy ? undefined : onClose} title={`Ocijeni rezervaciju #${reservation?.id}`} size="md" closeOnBackdrop={!busy}>
      <form onSubmit={handleSubmit}>
        {CATEGORIES.map((c) => (
          <div key={c.key} className="review-category-row">
            <span className="review-category-label">{c.label}</span>
            <StarRating value={ratings[c.key]} onChange={(v) => setRating(c.key, v)} size="lg" />
          </div>
        ))}

        <div className="form-group" style={{ marginTop: '16px' }}>
          <label>Komentar (opcionalno):</label>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows={4}
            placeholder="Podijelite svoje iskustvo..."
            style={{ width: '100%', resize: 'vertical' }}
          />
        </div>

        {error && <div className="modal-error">{error}</div>}

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onClose} disabled={busy}>Otkaži</button>
          <button type="submit" className="btn-primary" disabled={busy || !allRated}>
            {busy ? 'Šaljem...' : 'Pošalji recenziju'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
