import '../../styles/StarRating.css'

/**
 * Zvjezdice za ocjenu. Ako je onChange prosljeđen → interaktivan (klik bira).
 * Inače read-only prikaz (value može biti decimalan, npr. 4.3).
 */
export default function StarRating({ value = 0, onChange, size = 'md', readOnly = false }) {
  const stars = [1, 2, 3, 4, 5]
  const interactive = !readOnly && typeof onChange === 'function'

  return (
    <span className={`star-rating star-${size} ${interactive ? 'star-interactive' : ''}`}>
      {stars.map((n) => {
        const filled = value >= n
        const half = !filled && value >= n - 0.5
        return (
          <span
            key={n}
            className={`star ${filled ? 'filled' : half ? 'half' : 'empty'}`}
            onClick={interactive ? () => onChange(n) : undefined}
            role={interactive ? 'button' : undefined}
            aria-label={interactive ? `${n} zvjezdica` : undefined}
          >
            {filled ? '★' : half ? '★' : '☆'}
          </span>
        )
      })}
    </span>
  )
}
