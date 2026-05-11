import { useEffect, useState } from 'react'
import { reservationApi } from '../api/reservationApi'
import { walletApi } from '../api/walletApi'
import { useAuthStore } from '../store/authStore'
import '../styles/Dashboard.css'

export default function Dashboard() {
  const { user, isAuthenticated } = useAuthStore()
  const [reservations, setReservations] = useState([])
  const [wallet, setWallet] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [walletError, setWalletError] = useState(null)

  useEffect(() => {
    if (!isAuthenticated || !user?.id) return

    const fetchAll = async () => {
      try {
        const [resData, walletData] = await Promise.allSettled([
          reservationApi.getByGuestId(user.id),
          walletApi.getByUserId(user.id)
        ])

        if (resData.status === 'fulfilled') {
          setReservations(Array.isArray(resData.value) ? resData.value : (resData.value.content || []))
        } else {
          setError('Greška pri učitavanju rezervacija')
        }

        if (walletData.status === 'fulfilled') {
          setWallet(walletData.value)
        } else {
          setWalletError('Wallet ne postoji za ovog korisnika')
        }
      } finally {
        setLoading(false)
      }
    }

    fetchAll()
  }, [isAuthenticated, user?.id])

  if (!isAuthenticated) {
    return <div className="dashboard"><p>Molimo prijavite se</p></div>
  }

  const statusClass = (status) => {
    const s = (status || '').toUpperCase()
    if (s === 'CONFIRMED' || s === 'COMPLETED' || s === 'ACTIVE') return 'status status-ok'
    if (s === 'CANCELLED') return 'status status-bad'
    return 'status status-pending'
  }

  return (
    <div className="dashboard">
      <h1>Dobrodošli, {user.email}!</h1>
      <p>Vaša uloga: <strong>{user.role}</strong></p>

      {/* Wallet kartica */}
      <section className="wallet-card">
        <h2>💳 Vaš novčanik</h2>
        {walletError && <p className="wallet-empty">{walletError}</p>}
        {wallet && (
          <p className="wallet-balance">
            <span className="amount">{Number(wallet.balance).toFixed(2)}</span>
            <span className="currency"> {wallet.currency}</span>
          </p>
        )}
        <p className="wallet-hint">
          Naplata se vrši automatski kada potvrdite rezervaciju (Saga choreography).
        </p>
      </section>

      <section className="reservations-section">
        <h2>📅 Moje Rezervacije</h2>

        {loading && <div className="loading">Učitavanje...</div>}
        {error && <div className="error">{error}</div>}

        {!loading && reservations.length === 0 && (
          <div className="no-data">Nemate rezervacija</div>
        )}

        <div className="reservations-list">
          {reservations.map((res) => (
            <div key={res.id} className="reservation-card">
              <div className="reservation-header">
                <h3>Rezervacija #{res.id}</h3>
                <span className={statusClass(res.status)}>{res.status || 'CREATED'}</span>
              </div>
              <p>Smještaj ID: {res.propertyId}</p>
              <p>Dolazak: {res.checkIn}</p>
              <p>Odlazak: {res.checkOut}</p>
              <p>Osobe: {res.numGuests}</p>
              <p className="price">{Number(res.totalPrice).toFixed(2)} BAM</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
