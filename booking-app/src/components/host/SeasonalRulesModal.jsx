import { useState, useEffect } from 'react'
import Modal from '../common/Modal'
import { propertyApi } from '../../api/propertyApi'
import { useToast } from '../common/ToastProvider'
import { todayLocalISO } from '../../utils/dates'

/**
 * F15 — Host upravlja sezonskim pravilima property-a.
 * Lista postojecih + forma za dodavanje + brisanje.
 */
export default function SeasonalRulesModal({ open, onClose, property }) {
  const { showToast } = useToast()
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(true)
  const [adding, setAdding] = useState(false)

  // Forma za novo pravilo
  const [name, setName] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [priceModifierPct, setPriceModifierPct] = useState(20)
  const [minNights, setMinNights] = useState(1)
  const [error, setError] = useState(null)

  const load = async () => {
    if (!property?.id) return
    setLoading(true)
    try {
      const data = await propertyApi.getSeasonalRules(property.id)
      setRules(data || [])
    } catch {
      setRules([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [property?.id])

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!name.trim() || !startDate || !endDate) {
      setError('Naziv, početak i kraj su obavezni')
      return
    }
    if (new Date(endDate) <= new Date(startDate)) {
      setError('Kraj sezone mora biti nakon početka')
      return
    }
    setError(null)
    setAdding(true)
    try {
      await propertyApi.addSeasonalRule(property.id, {
        name: name.trim(),
        startDate,
        endDate,
        priceModifierPct: Number(priceModifierPct) || 0,
        minNights: Number(minNights) || 1
      })
      showToast({ type: 'success', title: 'Pravilo dodano', message: `Sezona "${name.trim()}" je aktivna.` })
      setName(''); setStartDate(''); setEndDate('')
      setPriceModifierPct(20); setMinNights(1)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri spremanju pravila')
    } finally {
      setAdding(false)
    }
  }

  const handleDelete = async (ruleId, ruleName) => {
    try {
      await propertyApi.deleteSeasonalRule(property.id, ruleId)
      showToast({ type: 'success', title: 'Obrisano', message: `Sezona "${ruleName}" je uklonjena.` })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Pravilo nije obrisano.' })
    }
  }

  const today = todayLocalISO()

  return (
    <Modal open={open} onClose={onClose} title={`Sezonska pravila: ${property?.name || ''}`} size="lg">
      {loading ? (
        <p className="modal-hint">Učitavanje...</p>
      ) : (
        <>
          <h4 style={{ marginBottom: '10px', color: 'var(--text-secondary)' }}>Postojeća pravila</h4>
          {rules.length === 0 ? (
            <p className="modal-hint">Nema definisanih sezona.</p>
          ) : (
            <div className="seasonal-rules-list">
              {rules.map((r) => (
                <div key={r.id} className="seasonal-rule-item">
                  <div className="seasonal-rule-info">
                    <strong>{r.name}</strong>
                    <span>{r.startDate} → {r.endDate}</span>
                    <span>{r.priceModifierPct > 0 ? '+' : ''}{r.priceModifierPct}% · min {r.minNights || 1} noći</span>
                  </div>
                  <button className="seasonal-rule-delete" onClick={() => handleDelete(r.id, r.name)}>🗑</button>
                </div>
              ))}
            </div>
          )}

          <h4 style={{ marginTop: '20px', marginBottom: '10px', color: 'var(--text-secondary)' }}>Dodaj novu sezonu</h4>
          <form onSubmit={handleAdd} className="seasonal-form">
            <div className="form-group">
              <label>Naziv (npr. "Ljeto 2026", "Praznici"):</label>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} disabled={adding} />
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Početak:</label>
                <input type="date" value={startDate} min={today} onChange={(e) => setStartDate(e.target.value)} disabled={adding} />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Kraj:</label>
                <input type="date" value={endDate} min={startDate || today} onChange={(e) => setEndDate(e.target.value)} disabled={adding} />
              </div>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Korekcija cijene (% — može biti negativna):</label>
                <input type="number" value={priceModifierPct} onChange={(e) => setPriceModifierPct(e.target.value)} disabled={adding} />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Min. noćenja:</label>
                <input type="number" min="1" value={minNights} onChange={(e) => setMinNights(e.target.value)} disabled={adding} />
              </div>
            </div>

            {error && <div className="modal-error">{error}</div>}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={onClose} disabled={adding}>Zatvori</button>
              <button type="submit" className="btn-primary" disabled={adding}>
                {adding ? 'Dodajem...' : '+ Dodaj sezonu'}
              </button>
            </div>
          </form>
        </>
      )}
    </Modal>
  )
}
