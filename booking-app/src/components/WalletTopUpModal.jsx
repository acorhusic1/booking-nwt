import { walletApi } from '../api/walletApi'
import PaymentCardModal from './common/PaymentCardModal'

/**
 * Top-up walleta kroz mock karticu — POST /api/wallets/{id}/deposit.
 * Card podaci se NE čuvaju backendu (samo simulacija); šalje se samo amount.
 */
export default function WalletTopUpModal({ wallet, onClose, onUpdated }) {
  const handlePay = async ({ amount }) => {
    const updated = await walletApi.deposit(wallet.id, amount)
    onUpdated(updated)
    onClose()
  }

  return (
    <PaymentCardModal
      open={true}
      onClose={onClose}
      onPay={handlePay}
      currency={wallet.currency}
      amountEditable
      amountLabel={`Trenutni balance: ${Number(wallet.balance).toFixed(2)} ${wallet.currency} — koliko dodati?`}
      payButtonLabel="Uplati"
      title="Dosipanje wallet-a karticom"
    />
  )
}
