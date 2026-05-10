import { Link } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import LogoutButton from '../auth/LogoutButton'
import '../../styles/Header.css'

export default function Header() {
  const { isAuthenticated, user } = useAuthStore()

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
