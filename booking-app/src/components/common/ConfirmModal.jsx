import { useState } from 'react'
import Modal from './Modal'

/**
 * Zamjena za native window.confirm() — u stilu aplikacije.
 *
 * Async pattern: onConfirm može biti async; pokazuje "Procesuirano..."
 * dok čeka. Ako onConfirm baci, error se prikazuje u modal-u i modal
 * ostaje otvoren (parent komponenta odlučuje šta dalje).
 */
export default function ConfirmModal({
  open,
  onClose,
  onConfirm,
  title = 'Potvrda',
  message,
  detail,
  confirmText = 'Potvrdi',
  cancelText = 'Otkaži',
  danger = false
}) {
  const [busy, setBusy] = useState(false)

  const handleConfirm = async () => {
    setBusy(true)
    try {
      await onConfirm?.()
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal open={open} onClose={busy ? undefined : onClose} title={title} size="sm" closeOnBackdrop={!busy}>
      <p className="confirm-message">{message}</p>
      {detail && <p className="confirm-detail">{detail}</p>}
      <div className="modal-actions">
        <button type="button" className="btn-secondary" onClick={onClose} disabled={busy}>
          {cancelText}
        </button>
        <button
          type="button"
          className={danger ? 'btn-danger' : 'btn-primary'}
          onClick={handleConfirm}
          disabled={busy}
        >
          {busy ? 'Procesuiranje...' : confirmText}
        </button>
      </div>
    </Modal>
  )
}
