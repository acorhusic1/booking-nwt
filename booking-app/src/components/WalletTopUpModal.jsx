import { useState } from 'react'
import { walletApi } from '../api/walletApi'

/**
 * Modal za top-up walleta — koristi POST /api/wallets/{id}/deposit.
 * Pure SPA: ne refreshuje stranicu, samo update-uje wallet u parent state-u
 * kroz onUpdated callback.
 */
export default function WalletTopUpModal({ wallet, onClose, onUpdated }) {
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    const num = Number(amount)
    if (!num || num <= 0) {
      setError('Iznos mora biti veći od 0')
      return
    }
    setLoading(true)
    try {
      const updated = await walletApi.deposit(wallet.id, num)
      onUpdated(updated)
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri uplati')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Dosipaj wallet</h3>
        <p className="modal-hint">Trenutni balance: <strong>{Number(wallet.balance).toFixed(2)} {wallet.currency}</strong></p>
        <form onSubmit={submit}>
          <input
            type="number"
            step="0.01"
            min="1"
            placeholder="Iznos u BAM"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            autoFocus
          />
          {error && <div className="modal-error">{error}</div>}
          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">Otkaži</button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Šaljem...' : 'Dosipaj'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
