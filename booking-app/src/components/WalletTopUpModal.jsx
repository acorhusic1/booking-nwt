import { useState } from 'react'
import { walletApi } from '../api/walletApi'
import PaymentCardModal from './common/PaymentCardModal'
import Modal from './common/Modal'
import { useToast } from './common/ToastProvider'

/**
 * Top-up walleta — dvije opcije:
 *   1. Mock kartica (PaymentCardModal) → backend deposit direktno
 *   2. Stripe Checkout → redirect na Stripe stranicu, callback verifikuje
 */
export default function WalletTopUpModal({ wallet, onClose, onUpdated }) {
  const { showToast } = useToast()
  const [view, setView] = useState('choose') // 'choose' | 'mock'
  const [stripeLoading, setStripeLoading] = useState(false)
  const [amount, setAmount] = useState('')
  const [error, setError] = useState(null)

  const handleMockPay = async ({ amount }) => {
    const updated = await walletApi.deposit(wallet.id, amount)
    onUpdated(updated)
    onClose()
  }

  const handleStripeCheckout = async () => {
    const num = Number(amount)
    if (!num || num < 1) {
      setError('Iznos mora biti barem 1 BAM')
      return
    }
    setError(null)
    setStripeLoading(true)
    try {
      const { url } = await walletApi.createStripeCheckout(wallet.id, num)
      if (!url) throw new Error('Stripe nije vratio URL')
      // Redirect na Stripe Checkout — Dashboard ce primiti callback sa ?stripe_session=
      window.location.href = url
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Stripe greška'
      setError(msg)
      showToast({ type: 'error', title: 'Stripe greška', message: msg })
      setStripeLoading(false)
    }
  }

  // Mock kartica view (postojeci flow)
  if (view === 'mock') {
    return (
      <PaymentCardModal
        open={true}
        onClose={onClose}
        onPay={handleMockPay}
        currency={wallet.currency}
        amountEditable
        amountLabel={`Trenutni balance: ${Number(wallet.balance).toFixed(2)} ${wallet.currency} — koliko dodati?`}
        payButtonLabel="Uplati"
        title="Dosipanje wallet-a (mock kartica)"
      />
    )
  }

  // Choose view (default) — bira između mock kartice i Stripe-a
  return (
    <Modal open={true} onClose={stripeLoading ? undefined : onClose} title="Dosipanje wallet-a" size="md" closeOnBackdrop={!stripeLoading}>
      <p className="modal-hint">
        Trenutni balance: <strong>{Number(wallet.balance).toFixed(2)} {wallet.currency}</strong>
      </p>

      <div className="payment-amount-display">
        <label htmlFor="topup-amount" className="label">Iznos za uplatu</label>
        <div className="amount-input-wrap">
          <input
            id="topup-amount"
            type="number"
            step="0.01"
            min="1"
            placeholder="0.00"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="amount-input"
            disabled={stripeLoading}
          />
          <span className="amount-currency">{wallet.currency}</span>
        </div>
      </div>

      {error && <div className="modal-error">{error}</div>}

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '20px' }}>
        <button
          type="button"
          className="btn-primary"
          onClick={handleStripeCheckout}
          disabled={stripeLoading || !amount}
          style={{ width: '100%', padding: '14px', fontSize: '0.95rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px' }}
        >
          {stripeLoading ? 'Preusmjeravam na Stripe...' : '💳 Plati karticom preko Stripe (test mode)'}
        </button>

        <div style={{ textAlign: 'center', color: 'var(--text-tertiary)', fontSize: '0.85rem' }}>— ili —</div>

        <button
          type="button"
          className="btn-secondary"
          onClick={() => { setView('mock'); setError(null); }}
          disabled={stripeLoading}
          style={{ width: '100%', padding: '12px', fontSize: '0.9rem' }}
        >
          Koristi mock karticu (bez pravog Stripe-a)
        </button>
      </div>

      <p className="payment-secure-note" style={{ marginTop: '16px' }}>
        🔒 Stripe Test Mode — koristi test karticu <code>4242 4242 4242 4242</code>, bilo koji budući datum i bilo koji CVV. Nikakav stvarni novac nije naplaćen.
      </p>
    </Modal>
  )
}
