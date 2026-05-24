import '../../styles/Spinner.css'

/**
 * CSS-only spinner u stilu aplikacije (violet). Tri varijante:
 *   - size: 'sm' | 'md' | 'lg'
 *   - inline: true → flex sa labelom pored umjesto centriranog blok
 *   - label: tekst desno od spinner-a
 */
export default function Spinner({ size = 'md', label = 'Učitavanje...', inline = false }) {
  return (
    <div className={`spinner-wrap ${inline ? 'spinner-inline' : ''}`} role="status" aria-live="polite">
      <div className={`spinner spinner-${size}`} aria-hidden="true" />
      {label && <span className="spinner-label">{label}</span>}
    </div>
  )
}
