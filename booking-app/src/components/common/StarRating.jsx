import '../../styles/StarRating.css'

/**
 * Zvjezdice za ocjenu. Ako je onChange prosljeđen → interaktivan (klik bira).
 * Inače read-only prikaz (value može biti decimalan, npr. 4.3).
 */
export default function StarRating({ value = 0, onChange, size = 'md', readOnly = false }) {
  const stars = [1, 2, 3, 4, 5]
  const interactive = !readOnly && typeof onChange === 'function'
  // BUG G — u read-only modu zaokruzujemo na najblizu cijelu zvjezdicu
  // (4.7 → 5, 4.1 → 4). Bez ovog se 4.1 i 4.9 prikazuju identicno (4 zvjezdice).
  const displayValue = interactive ? value : Math.round(Number(value) || 0)

  return (
    <span className={`star-rating star-${size} ${interactive ? 'star-interactive' : ''}`}>
      {stars.map((n) => {
        const filled = displayValue >= n
        const half = !filled && displayValue >= n - 0.5
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
