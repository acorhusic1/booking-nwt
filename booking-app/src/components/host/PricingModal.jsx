import { useState, useEffect } from 'react'
import Modal from '../common/Modal'
import { propertyApi } from '../../api/propertyApi'
import { useToast } from '../common/ToastProvider'

/**
 * F4 — Host postavlja cijene za property (basePrice, weekendPrice,
 * long-stay popust). Backend PUT /api/properties/{id}/pricing.
 */
export default function PricingModal({ open, onClose, property, onSaved }) {
  const { showToast } = useToast()
  const [basePrice, setBasePrice] = useState('')
  const [weekendPrice, setWeekendPrice] = useState('')
  const [minStayDays, setMinStayDays] = useState(1)
  const [maxStayDays, setMaxStayDays] = useState(30)
  const [longStayThreshold, setLongStayThreshold] = useState(7)
  const [longStayDiscountPct, setLongStayDiscountPct] = useState(10)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  // Učitaj postojeci pricing (ako postoji)
  useEffect(() => {
    if (!property?.id) return
    setLoading(true)
    propertyApi.getPricing(property.id)
      .then((p) => {
        if (p) {
          setBasePrice(p.basePrice ?? '')
          setWeekendPrice(p.weekendPrice ?? '')
          setMinStayDays(p.minStayDays ?? 1)
          setMaxStayDays(p.maxStayDays ?? 30)
          setLongStayThreshold(p.longStayThreshold ?? 7)
          setLongStayDiscountPct(p.longStayDiscountPct ?? 10)
        }
      })
      .catch(() => {/* nema pricing — koristi defaults */})
      .finally(() => setLoading(false))
  }, [property?.id])

  const handleSave = async (e) => {
    e.preventDefault()
    const base = Number(basePrice)
    if (!base || base <= 0) {
      setError('Bazna cijena mora biti veća od 0')
      return
    }
    setError(null)
    setSaving(true)
    try {
      await propertyApi.updatePricing(property.id, {
        basePrice: base,
        weekendPrice: Number(weekendPrice) || base,
        minStayDays: Number(minStayDays) || 1,
        maxStayDays: Number(maxStayDays) || 30,
        longStayThreshold: Number(longStayThreshold) || 0,
        longStayDiscountPct: Number(longStayDiscountPct) || 0
      })
      showToast({ type: 'success', title: 'Cijene spremljene', message: `Pricing za "${property.name}" je ažuriran.` })
      onSaved?.()
      onClose()
    } catch (err) {
      const msg = err.response?.data?.message || 'Greška pri spremanju cijena'
      setError(msg)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={saving ? undefined : onClose}
           title={`Cijene: ${property?.name || ''}`}
           size="md" closeOnBackdrop={!saving}>
      {loading ? (
        <p className="modal-hint">Učitavanje...</p>
      ) : (
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label>Bazna cijena (radni dan, BAM):</label>
            <input type="number" step="0.01" min="1" value={basePrice}
                   onChange={(e) => setBasePrice(e.target.value)} disabled={saving} />
          </div>

          <div className="form-group">
            <label>Vikend cijena (Sub/Ned, BAM):</label>
            <input type="number" step="0.01" min="1" value={weekendPrice}
                   onChange={(e) => setWeekendPrice(e.target.value)} disabled={saving}
                   placeholder={`Ako prazno → ${basePrice || 'bazna'}`} />
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Min. noći:</label>
              <input type="number" min="1" value={minStayDays}
                     onChange={(e) => setMinStayDays(e.target.value)} disabled={saving} />
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Max. noći:</label>
              <input type="number" min="1" value={maxStayDays}
                     onChange={(e) => setMaxStayDays(e.target.value)} disabled={saving} />
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Long-stay prag (noći):</label>
              <input type="number" min="0" value={longStayThreshold}
                     onChange={(e) => setLongStayThreshold(e.target.value)} disabled={saving} />
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Long-stay popust (%):</label>
              <input type="number" min="0" max="100" value={longStayDiscountPct}
                     onChange={(e) => setLongStayDiscountPct(e.target.value)} disabled={saving} />
            </div>
          </div>

          <p className="modal-hint" style={{ marginTop: '10px' }}>
            Primjer: bazna {basePrice || '?'} BAM, vikend {weekendPrice || basePrice || '?'} BAM, popust {longStayDiscountPct || 0}% za boravak {longStayThreshold || 0}+ noći.
          </p>

          {error && <div className="modal-error">{error}</div>}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={saving}>Otkaži</button>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Spremam...' : 'Spremi cijene'}
            </button>
          </div>
        </form>
      )}
    </Modal>
  )
}
