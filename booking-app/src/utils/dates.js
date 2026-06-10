/**
 * Lokalno formatiranje datuma u YYYY-MM-DD.
 *
 * VAŽNO: ne koristiti `date.toISOString().split('T')[0]` za lokalne datume!
 * toISOString() konvertuje u UTC pa u našoj zoni (UTC+1/+2) lokalna ponoć
 * postane prethodni dan 22:00/23:00 UTC — datum se pomjeri za -1 dan.
 * (Bug: gost izabere 10–15, a forma pošalje 9–14.)
 */
export function toLocalISO(date) {
  const d = date instanceof Date ? date : new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** Današnji datum u lokalnoj zoni kao YYYY-MM-DD (za min atribute inputa). */
export function todayLocalISO() {
  return toLocalISO(new Date())
}
