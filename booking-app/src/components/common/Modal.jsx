import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import '../../styles/Modal.css'

/**
 * Reusable modal wrapper. Backdrop click + Escape zatvaraju modal.
 * Renderuje se kroz React portal u document.body — bitno zbog
 * `position: fixed` koji se lomi unutar elemenata sa `transform`
 * (hover translateY na reservation-card je to "uhvatio" prije fixa).
 */
export default function Modal({ open, onClose, title, children, size = 'md', closeOnBackdrop = true }) {
  useEffect(() => {
    if (!open) return
    const onKey = (e) => { if (e.key === 'Escape') onClose?.() }
    document.addEventListener('keydown', onKey)
    const prev = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = prev
    }
  }, [open, onClose])

  if (!open) return null

  // VAZNO: React portali propagiraju event kroz React tree (ne DOM tree).
  // Bez stopPropagation, svaki klik unutar modala bubbla do roditeljskih komponenata
  // (npr. ReservationCard onClick={toggle}) → kartica flickera dok je modal otvoren.
  const handleBackdrop = (e) => {
    e.stopPropagation()
    if (closeOnBackdrop && e.target === e.currentTarget) onClose?.()
  }

  const stopInsideClicks = (e) => e.stopPropagation()

  const node = (
    <div className="modal-overlay" onClick={handleBackdrop}>
      <div className={`modal modal-${size}`} role="dialog" aria-modal="true"
           aria-labelledby="modal-title" onClick={stopInsideClicks}>
        {title && (
          <div className="modal-header">
            <h3 id="modal-title">{title}</h3>
            <button type="button" className="modal-close" onClick={onClose} aria-label="Zatvori">×</button>
          </div>
        )}
        <div className="modal-body">{children}</div>
      </div>
    </div>
  )

  return createPortal(node, document.body)
}
