import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { notificationApi } from '../../api/notificationApi'
import { useAuthStore } from '../../store/authStore'
import Spinner from '../common/Spinner'
import ErrorState from '../common/ErrorState'

const TYPE_META = {
  POTVRDA_REZERVACIJE: { icon: '✅', label: 'Potvrđeno' },
  OTKAZANA_REZERVACIJA: { icon: '❌', label: 'Otkazano' },
  NOVA_REZERVACIJA:     { icon: '📋', label: 'Nova rezervacija' },
  ZAHTJEV_ZA_RECENZIJU: { icon: '⭐', label: 'Recenzija' },
  NOVA_PORUKA:          { icon: '💬', label: 'Poruka' },
  PODSJETNIK:           { icon: '🔔', label: 'Podsjetnik' },
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('bs-BA', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

export default function NotificationsList({ onUnreadChange }) {
  const { user } = useAuthStore()
  // BUG C — klik na notifikaciju je ranije vodio na /properties/<id> (lista smjestaja
  // kad propertyId nije bio postavljen). Sad linkamo direktno na korisnikove
  // rezervacije, sto je smisao notifikacije.
  const reservationsHref = user?.role === 'HOST' ? '/host/dashboard' : '/dashboard?tab=reservations'
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // BUG: na svaki poll-driven load setovali smo loading=true → cijela lista bi se
  // zamijenila Spinner-om i scroll bi skocio na vrh stranice. Sad poll radi "tiho":
  // samo prvi mount pokazuje Spinner, polling u pozadini samo apdejtuje state.
  const load = (showSpinner = false) => {
    if (!user?.id) return
    if (showSpinner) setLoading(true)
    setError(null)
    notificationApi.getByUserId(user.id)
      .then(data => {
        const list = Array.isArray(data) ? data : (data.content || [])
        setNotifications(list)
        const unread = list.filter(n => !n.isRead).length
        onUnreadChange?.(unread)
      })
      .catch(() => setError('Greška pri učitavanju notifikacija'))
      .finally(() => { if (showSpinner) setLoading(false) })
  }

  useEffect(() => {
    load(true)
    // Tihi poll svakih 15s — bez setLoading da ne unmountuje listu i ne skace scroll
    const t = setInterval(() => load(false), 15000)
    return () => clearInterval(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id])

  const handleMarkRead = async (id) => {
    try {
      await notificationApi.markAsRead(id)
      setNotifications(prev => {
        const updated = prev.map(n => n.id === id ? { ...n, isRead: true } : n)
        const unread = updated.filter(n => !n.isRead).length
        onUnreadChange?.(unread)
        return updated
      })
    } catch {
      // ne blokiramo UI
    }
  }

  if (loading) return <Spinner label="Učitavanje notifikacija..." />
  if (error)   return <ErrorState message={error} onRetry={load} />

  if (notifications.length === 0) {
    return (
      <div className="no-data">
        Nemate notifikacija
      </div>
    )
  }

  return (
    <div className="notifications-list">
      {notifications.map(n => {
        const meta = TYPE_META[n.type] || { icon: '🔔', label: n.type }
        const isSuccess = n.type === 'POTVRDA_REZERVACIJE'
        const isFail    = n.type === 'OTKAZANA_REZERVACIJA'

        return (
          <div
            key={n.id}
            className={`notification-card ${!n.isRead ? 'unread' : ''} ${isSuccess ? 'success' : ''} ${isFail ? 'fail' : ''}`}
          >
            <div className="notif-left">
              <span className="notif-icon">{meta.icon}</span>
            </div>

            <div className="notif-body">
              <div className="notif-header">
                <span className="notif-type">{meta.label}</span>
                <span className="notif-date">{formatDate(n.createdAt)}</span>
              </div>
              <p className="notif-title">{n.title}</p>
              <p className="notif-content">{n.content}</p>

              <div className="notif-actions">
                {n.type === 'NOVA_PORUKA' ? (
                  // Notif za novu poruku vodi direktno u Messages, ne na rezervaciju
                  <Link
                    to="/messages"
                    className="notif-link"
                    onClick={() => !n.isRead && handleMarkRead(n.id)}
                  >
                    Otvori poruke →
                  </Link>
                ) : n.relatedReservationId && (
                  <Link
                    to={reservationsHref}
                    className="notif-link"
                    onClick={() => !n.isRead && handleMarkRead(n.id)}
                  >
                    Rezervacija #{n.relatedReservationId} →
                  </Link>
                )}
                {!n.isRead && (
                  <button className="notif-read-btn" onClick={() => handleMarkRead(n.id)}>
                    Označi kao pročitano
                  </button>
                )}
              </div>
            </div>

            {!n.isRead && <span className="unread-dot" />}
          </div>
        )
      })}
    </div>
  )
}
