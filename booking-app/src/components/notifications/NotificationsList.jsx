import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { notificationApi } from '../../api/notificationApi'
import { useAuthStore } from '../../store/authStore'

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
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!user?.id) return
    notificationApi.getByUserId(user.id)
      .then(data => {
        const list = Array.isArray(data) ? data : (data.content || [])
        setNotifications(list)
        const unread = list.filter(n => !n.isRead).length
        onUnreadChange?.(unread)
      })
      .catch(() => setError('Greška pri učitavanju notifikacija'))
      .finally(() => setLoading(false))
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

  if (loading) return <div className="loading">Učitavanje notifikacija...</div>
  if (error)   return <div className="error">{error}</div>

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
                {n.relatedReservationId && (
                  <Link
                    to={`/properties/${n.relatedPropertyId || ''}`}
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
