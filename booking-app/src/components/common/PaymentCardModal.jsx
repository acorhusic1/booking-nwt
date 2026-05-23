import { useState } from 'react'
import Modal from './Modal'

/**
 * Stripe-style modal za unos kartice. Klijentska validacija — backend
 * ne čuva broj kartice (mock plaćanje). Last4 se prosljeđuje gore za
 * prikaz u istoriji.
 *
 * Format kartice: "4242 4242 4242 4242" (4-4-4-4 sa razmacima)
 * Expiry: "MM/YY"
 * CVV: 3-4 cifre
 */
export default function PaymentCardModal({
  open,
  onClose,
  onPay,
  amount,
  currency = 'BAM',
  title = 'Plaćanje karticom',
  amountEditable = false,
  amountLabel = 'Iznos za plaćanje',
  payButtonLabel = 'Plati'
}) {
  const [number, setNumber] = useState('')
  const [name, setName] = useState('')
  const [expiry, setExpiry] = useState('')
  const [cvv, setCvv] = useState('')
  const [editableAmount, setEditableAmount] = useState(amount != null ? String(amount) : '')
  const [errors, setErrors] = useState({})
  const [busy, setBusy] = useState(false)
  const [serverError, setServerError] = useState(null)

  const effectiveAmount = amountEditable ? Number(editableAmount) : amount

  const formatNumber = (raw) => raw.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim()
  const formatExpiry = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 4)
    if (digits.length < 3) return digits
    return digits.slice(0, 2) + '/' + digits.slice(2)
  }

  const validate = () => {
    const e = {}
    const digits = number.replace(/\s/g, '')
    if (digits.length !== 16) e.number = 'Broj kartice mora imati 16 cifara'
    if (!name.trim() || name.trim().length < 3) e.name = 'Ime vlasnika je obavezno'
    if (amountEditable) {
      const n = Number(editableAmount)
      if (!n || n <= 0) e.amount = 'Iznos mora biti veći od 0'
    }

    if (!/^\d{2}\/\d{2}$/.test(expiry)) {
      e.expiry = 'Format MM/YY'
    } else {
      const [mm, yy] = expiry.split('/').map(Number)
      if (mm < 1 || mm > 12) e.expiry = 'Mjesec nevažeći (01-12)'
      else {
        const now = new Date()
        const expDate = new Date(2000 + yy, mm - 1, 1)
        const thisMonth = new Date(now.getFullYear(), now.getMonth(), 1)
        if (expDate < thisMonth) e.expiry = 'Kartica je istekla'
      }
    }

    if (!/^\d{3,4}$/.test(cvv)) e.cvv = 'CVV ima 3-4 cifre'

    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (ev) => {
    ev.preventDefault()
    setServerError(null)
    if (!validate()) return
    setBusy(true)
    try {
      const last4 = number.replace(/\s/g, '').slice(-4)
      await onPay?.({ last4, cardholderName: name.trim(), amount: effectiveAmount })
    } catch (err) {
      const msg = err.response?.data?.message
      setServerError(typeof msg === 'string' ? msg : err.message || 'Plaćanje nije uspjelo')
    } finally {
      setBusy(false)
    }
  }

  const reset = () => {
    setNumber(''); setName(''); setExpiry(''); setCvv('')
    setEditableAmount(amount != null ? String(amount) : '')
    setErrors({}); setServerError(null)
  }

  const handleClose = () => {
    if (busy) return
    reset()
    onClose?.()
  }

  return (
    <Modal open={open} onClose={handleClose} title={title} size="md" closeOnBackdrop={!busy}>
      <div className="payment-card-preview">
        <div className="payment-card-chip" />
        <div className="payment-card-number">
          {number || '•••• •••• •••• ••••'}
        </div>
        <div className="payment-card-row">
          <div>
            <div className="label">Vlasnik</div>
            <div className="value">{name.toUpperCase() || 'IME PREZIME'}</div>
          </div>
          <div>
            <div className="label">Ističe</div>
            <div className="value">{expiry || 'MM/YY'}</div>
          </div>
        </div>
      </div>

      {amountEditable ? (
        <div className="payment-amount-display">
          <label htmlFor="topup-amount" className="label">{amountLabel}</label>
          <div className="amount-input-wrap">
            <input
              id="topup-amount"
              type="number"
              step="0.01"
              min="1"
              placeholder="0.00"
              value={editableAmount}
              onChange={(e) => setEditableAmount(e.target.value)}
              className="amount-input"
            />
            <span className="amount-currency">{currency}</span>
          </div>
          {errors.amount && <div className="field-error">{errors.amount}</div>}
        </div>
      ) : (
        amount != null && (
          <div className="payment-amount-display">
            <span className="label">{amountLabel}</span>
            <span className="amount">{Number(amount).toFixed(2)} {currency}</span>
          </div>
        )
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div className="payment-form-grid">
          <div className="full">
            <label htmlFor="card-number">Broj kartice</label>
            <input
              id="card-number"
              type="text"
              inputMode="numeric"
              autoComplete="cc-number"
              placeholder="4242 4242 4242 4242"
              value={number}
              onChange={(e) => setNumber(formatNumber(e.target.value))}
              maxLength={19}
              autoFocus
            />
            {errors.number && <div className="field-error">{errors.number}</div>}
          </div>

          <div className="full">
            <label htmlFor="card-name">Ime vlasnika</label>
            <input
              id="card-name"
              type="text"
              autoComplete="cc-name"
              placeholder="IME PREZIME"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            {errors.name && <div className="field-error">{errors.name}</div>}
          </div>

          <div>
            <label htmlFor="card-exp">Datum isteka</label>
            <input
              id="card-exp"
              type="text"
              inputMode="numeric"
              autoComplete="cc-exp"
              placeholder="MM/YY"
              value={expiry}
              onChange={(e) => setExpiry(formatExpiry(e.target.value))}
              maxLength={5}
            />
            {errors.expiry && <div className="field-error">{errors.expiry}</div>}
          </div>

          <div>
            <label htmlFor="card-cvv">CVV</label>
            <input
              id="card-cvv"
              type="text"
              inputMode="numeric"
              autoComplete="cc-csc"
              placeholder="•••"
              value={cvv}
              onChange={(e) => setCvv(e.target.value.replace(/\D/g, '').slice(0, 4))}
              maxLength={4}
            />
            {errors.cvv && <div className="field-error">{errors.cvv}</div>}
          </div>
        </div>

        {serverError && <div className="modal-error">{serverError}</div>}

        <div className="payment-secure-note">
          🔒 Podaci kartice se ne čuvaju — koristi se samo last4 za referencu.
        </div>

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={handleClose} disabled={busy}>
            Odustani
          </button>
          <button type="submit" className="btn-primary" disabled={busy}>
            {busy
              ? 'Procesuiranje...'
              : `${payButtonLabel}${effectiveAmount > 0 ? ` ${Number(effectiveAmount).toFixed(2)} ${currency}` : ''}`}
          </button>
        </div>
      </form>
    </Modal>
  )
}
