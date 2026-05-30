import { useState } from 'react'
import Modal from '../common/Modal'
import { problemReportApi } from '../../api/problemReportApi'
import { useToast } from '../common/ToastProvider'

/**
 * F17 — Modal za prijavu problema tokom boravka.
 */
const CATEGORIES = [
  'KVAR_UREDJAJA',
  'CISTOCA',
  'NEUSKLAÐENOST_OPISA',
  'BUKA',
  'SIGURNOST',
  'OSTALO'
]

const CATEGORY_LABELS = {
  KVAR_UREDJAJA: 'Kvar uređaja',
  CISTOCA: 'Čistoća',
  'NEUSKLAÐENOST_OPISA': 'Neusklađenost sa opisom',
  BUKA: 'Buka',
  SIGURNOST: 'Sigurnost',
  OSTALO: 'Ostalo'
}

export default function ProblemReportModal({ open, onClose, reservation, reporterId, onSubmitted }) {
  const { showToast } = useToast()
  const [category, setCategory] = useState(CATEGORIES[0])
  const [description, setDescription] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (description.trim().length < 10) {
      setError('Opis mora imati barem 10 karaktera')
      return
    }
    setError(null)
    setBusy(true)
    try {
      await problemReportApi.create(reservation.id, reporterId, category, description.trim())
      showToast({ type: 'success', title: 'Prijava poslana',
        message: 'Domaćin/administrator je obaviješten i odgovorit će u najkraćem roku.' })
      onSubmitted?.()
      onClose()
    } catch (err) {
      const msg = err.response?.data?.message || 'Greška pri slanju prijave'
      setError(msg)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal open={open} onClose={busy ? undefined : onClose}
           title={`Prijava problema (Rezervacija #${reservation?.id})`}
           size="md" closeOnBackdrop={!busy}>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Kategorija:</label>
          <select value={category} onChange={(e) => setCategory(e.target.value)} disabled={busy}>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>{CATEGORY_LABELS[c]}</option>
            ))}
          </select>
        </div>

        <div className="form-group" style={{ marginTop: '14px' }}>
          <label>Opis problema:</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={5}
            placeholder="Opišite problem detaljno (minimum 10 karaktera)..."
            style={{ width: '100%', resize: 'vertical' }}
            disabled={busy}
          />
        </div>

        {error && <div className="modal-error">{error}</div>}

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onClose} disabled={busy}>Otkaži</button>
          <button type="submit" className="btn-primary" disabled={busy || description.trim().length < 10}>
            {busy ? 'Šaljem...' : 'Pošalji prijavu'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
