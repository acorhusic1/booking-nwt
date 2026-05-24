import '../../styles/ErrorState.css'

/**
 * Centralizovani prikaz greske sa Retry dugmetom. Zamjenjuje
 * <div className="error"> sirom aplikacije.
 *
 * Ako se proslijedi onRetry callback, prikazuje se dugme. Inace samo
 * tekst greske (npr. validacioni error koji se ne moze ponovo pokusati).
 */
export default function ErrorState({ message, onRetry, retryLabel = 'Pokušaj ponovo', inline = false }) {
  if (!message) return null

  return (
    <div className={`error-state ${inline ? 'error-state-inline' : ''}`} role="alert">
      <span className="error-state-icon" aria-hidden="true">⚠</span>
      <div className="error-state-body">
        <p className="error-state-message">{message}</p>
        {onRetry && (
          <button type="button" className="error-state-retry" onClick={onRetry}>
            ↻ {retryLabel}
          </button>
        )}
      </div>
    </div>
  )
}
