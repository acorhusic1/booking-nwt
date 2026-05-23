import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import '../../styles/Toast.css'

/**
 * Toast notifikacija (gornji desni ugao). Portal u document.body da
 * ne dijeli stacking context sa karticama. Auto-dismiss nakon `duration` ms.
 *
 * type: 'success' | 'error' | 'info' | 'warning'
 */
export default function Toast({ open, onClose, type = 'info', title, message, duration = 5000 }) {
  useEffect(() => {
    if (!open || !duration) return
    const t = setTimeout(() => onClose?.(), duration)
    return () => clearTimeout(t)
  }, [open, duration, onClose])

  if (!open) return null

  const icon = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  }[type] || 'ℹ'

  return createPortal(
    <div className={`toast toast-${type}`} role="alert">
      <span className="toast-icon">{icon}</span>
      <div className="toast-body">
        {title && <div className="toast-title">{title}</div>}
        <div className="toast-message">{message}</div>
      </div>
      <button type="button" className="toast-close" onClick={onClose} aria-label="Zatvori">×</button>
    </div>,
    document.body
  )
}
