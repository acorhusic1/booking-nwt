import { useEffect, useRef, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import { reservationApi } from '../api/reservationApi'
import { walletApi } from '../api/walletApi'
import { useAuthStore } from '../store/authStore'
import ReservationCard from './reservations/ReservationCard'
import WalletTopUpModal from './WalletTopUpModal'
import NotificationsList from './notifications/NotificationsList'
import '../styles/Dashboard.css'

/**
 * SPA Dashboard — wallet + rezervacije + notifikacije ulogovanog korisnika.
 *
 * Kombinuje:
 *   - Kenan: tabovi (Rezervacije / Notifikacije) + notification poll na ?pending=1
 *   - Benjamin: ReservationCard sa expand/cancel/payments, WalletTopUpModal,
 *     auto-poll rezervacija na ?pendingId=N (uhvati Saga rezultat bez F5)
 *
 * SPA principi:
 *   1. Sve podatke dohvata kroz REST (Promise.allSettled) — bez SSR-a.
 *   2. Tabovi se mijenjaju bez page reload-a (state-based switching).
 *   3. Auto-poll: ?pendingId=N → osvježi rezervacije, ?pending=1 → osvježi notifikacije.
 *   4. Cancel/top-up update-uju samo dio state-a kroz API, bez full reload-a.
 *
 * Sigurnost: apiClient automatski šalje JWT u Authorization headeru.
 * Ako token istekne (401), apiClient redirektuje na /login.
 */
export default function Dashboard() {
  const { user, isAuthenticated } = useAuthStore()
  const [searchParams, setSearchParams] = useSearchParams()
  const pendingId = searchParams.get('pendingId')

  // Tabovi (Kenan)
  const [activeTab, setActiveTab] = useState(
    searchParams.get('tab') === 'notifications' ? 'notifications' : 'reservations'
  )
  const [unreadCount, setUnreadCount] = useState(0)
  const [sagaPending, setSagaPending] = useState(searchParams.get('pending') === '1')
  const [notifKey, setNotifKey] = useState(0)
  const notifPollRef = useRef(null)

  // Reservations + wallet (Benjamin)
  const [reservations, setReservations] = useState([])
  const [wallet, setWallet] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [walletError, setWalletError] = useState(null)
  const [showTopUp, setShowTopUp] = useState(false)
  const [sagaPolling, setSagaPolling] = useState(false)
  const resPollRef = useRef(null)

  // Notification auto-poll (Kenan grana) — refresh NotificationsList 3× kad
  // se vrati sa rezervacije sa ?pending=1
  useEffect(() => {
    if (!sagaPending) return
    let count = 0
    notifPollRef.current = setInterval(() => {
      setNotifKey(k => k + 1)
      count++
      if (count >= 3) {
        clearInterval(notifPollRef.current)
        setSagaPending(false)
      }
    }, 3000)
    return () => clearInterval(notifPollRef.current)
  }, [sagaPending])

  // Glavni fetch (rezervacije + wallet) — Benjamin grana
  const fetchAll = useCallback(async () => {
    if (!isAuthenticated || !user?.id) return
    try {
      const [resData, walletData] = await Promise.allSettled([
        reservationApi.getByGuestId(user.id),
        walletApi.getByUserId(user.id)
      ])
      if (resData.status === 'fulfilled') {
        setReservations(Array.isArray(resData.value) ? resData.value : (resData.value.content || []))
        setError(null)
      } else {
        setError('Greška pri učitavanju rezervacija')
      }
      if (walletData.status === 'fulfilled') {
        setWallet(walletData.value)
        setWalletError(null)
      } else {
        setWalletError('Wallet ne postoji za ovog korisnika')
      }
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated, user?.id])

  useEffect(() => { fetchAll() }, [fetchAll])

  // Reservation auto-poll (Benjamin grana) — uhvati Saga rezultat bez F5
  useEffect(() => {
    if (!pendingId) return
    setSagaPolling(true)
    let count = 0
    resPollRef.current = setInterval(async () => {
      count++
      await fetchAll()
      const target = reservations.find(r => String(r.id) === pendingId)
      if ((target && target.status !== 'CREATED') || count >= 5) {
        clearInterval(resPollRef.current)
        setSagaPolling(false)
        searchParams.delete('pendingId')
        setSearchParams(searchParams, { replace: true })
      }
    }, 2000)
    return () => clearInterval(resPollRef.current)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pendingId])

  if (!isAuthenticated) {
    return <div className="dashboard"><p>Molimo prijavite se</p></div>
  }

  return (
    <div className="dashboard">
      <h1>Dobrodošli, {user.email}!</h1>
      <p>Vaša uloga: <strong>{user.role}</strong></p>

      {/* Wallet kartica */}
      <section className="wallet-card">
        <div className="wallet-card-header">
          <h2>💳 Vaš novčanik</h2>
          {wallet && (
            <button onClick={() => setShowTopUp(true)} className="topup-btn">
              + Dosipaj
            </button>
          )}
        </div>
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

      {/* Tabovi (Kenan) */}
      <div className="dashboard-tabs">
        <button
          className={`tab-btn ${activeTab === 'reservations' ? 'active' : ''}`}
          onClick={() => setActiveTab('reservations')}
        >
          📅 Rezervacije
        </button>
        <button
          className={`tab-btn ${activeTab === 'notifications' ? 'active' : ''}`}
          onClick={() => setActiveTab('notifications')}
        >
          🔔 Notifikacije
          {unreadCount > 0 && (
            <span className="tab-badge">{unreadCount}</span>
          )}
        </button>
      </div>

      {/* Tab: Rezervacije */}
      {activeTab === 'reservations' && (
        <section className="reservations-section">
          <div className="reservations-header">
            <h2>📅 Moje Rezervacije</h2>
            <button onClick={fetchAll} className="refresh-btn" title="Osvježi listu">↻ Osvježi</button>
          </div>

          {sagaPolling && (
            <div className="saga-pending-banner">
              ⏳ Saga je u toku — čekamo rezultat naplate za rezervaciju #{pendingId}…
            </div>
          )}

          {loading && <div className="loading">Učitavanje...</div>}
          {error && <div className="error">{error}</div>}

          {!loading && reservations.length === 0 && (
            <div className="no-data">Nemate rezervacija</div>
          )}

          <div className="reservations-list">
            {reservations.map(res => (
              <ReservationCard
                key={res.id}
                reservation={res}
                onChanged={fetchAll}
              />
            ))}
          </div>
        </section>
      )}

      {/* Tab: Notifikacije (Kenan) */}
      {activeTab === 'notifications' && (
        <section className="notifications-section">
          {sagaPending && (
            <div className="saga-pending-banner">
              ⏳ Rezervacija kreirana — plaćanje se obrađuje. Notifikacija će se pojaviti za nekoliko sekundi…
            </div>
          )}
          <NotificationsList key={notifKey} onUnreadChange={setUnreadCount} />
        </section>
      )}

      {showTopUp && wallet && (
        <WalletTopUpModal
          wallet={wallet}
          onClose={() => setShowTopUp(false)}
          onUpdated={setWallet}
        />
      )}
    </div>
  )
}
