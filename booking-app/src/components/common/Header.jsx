import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuthStore } from '../../store/authStore'
import { notificationApi } from '../../api/notificationApi'
import LogoutButton from '../auth/LogoutButton'
import '../../styles/Header.css'

export default function Header() {
  const { isAuthenticated, user } = useAuthStore()
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    if (!isAuthenticated || !user?.id) return
    const fetchUnread = () => {
      notificationApi.countUnread(user.id)
        .then(count => setUnread(Number(count) || 0))
        .catch(() => {})
    }
    fetchUnread()
    // Polling svakih 8s — bez ovog badge se updateuje tek na page refresh.
    // 8s je dovoljno brzo za "live" osjecaj, sporo dovoljno da ne tuce backend.
    const interval = setInterval(fetchUnread, 8000)
    return () => clearInterval(interval)
  }, [isAuthenticated, user?.id])

  return (
    <header className="header">
      <div className="header-content">
        <Link to="/" className="logo">
          🏠 BookingApp
        </Link>

        <nav className="nav">
          <Link to="/">Početna</Link>
          <Link to="/properties">Smještaji</Link>

          {isAuthenticated ? (
            <>
              <Link to="/dashboard">Dashboard</Link>
              {user?.role === 'ADMIN' && (
                <Link to="/admin" className="nav-admin">⚙️ Admin</Link>
              )}
              {user?.role === 'HOST' && (
                <Link to="/host/dashboard" className="nav-host">🏘️ Moji smještaji</Link>
              )}
              {user?.role === 'GUEST' && (
                <Link to="/wishlist" className="nav-wishlist">❤️ Liste želja</Link>
              )}
              {(user?.role === 'GUEST' || user?.role === 'HOST') && (
                <Link to="/messages" className="nav-messages">💬 Poruke</Link>
              )}
              <Link to="/dashboard?tab=notifications" className="nav-bell" title="Notifikacije">
                🔔
                {unread > 0 && <span className="bell-badge">{unread > 9 ? '9+' : unread}</span>}
              </Link>
              <Link to="/profile" className="nav-profile">👤 Profil</Link>
              <span className="user-info">
                {user?.email} ({user?.role})
              </span>
              <LogoutButton />
            </>
          ) : (
            <>
              <Link to="/login" className="btn-login">Prijava</Link>
              <Link to="/register" className="btn-register">Registracija</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
