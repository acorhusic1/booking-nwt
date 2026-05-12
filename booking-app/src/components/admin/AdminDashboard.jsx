import { useState, useEffect } from 'react'
import { userApi } from '../../api/userApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import '../../styles/AdminDashboard.css'

export default function AdminDashboard() {
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalUsers, setTotalUsers] = useState(0)
  const [deleteConfirm, setDeleteConfirm] = useState(null)

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      navigate('/dashboard')
      return
    }
    fetchUsers()
  }, [page, user?.role])

  const fetchUsers = async () => {
    setLoading(true)
    try {
      const data = await userApi.getAll(page, 10)
      setUsers(data.content || [])
      setTotalPages(data.totalPages || 1)
      setTotalUsers(data.totalElements || 0)
      setError(null)
    } catch (err) {
      setError('Greška pri učitavanju korisnika')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (userId) => {
    try {
      await userApi.delete(userId)
      setDeleteConfirm(null)
      fetchUsers()
    } catch (err) {
      setError('Greška pri brisanju korisnika')
    }
  }

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'ADMIN': return 'badge-admin'
      case 'HOST': return 'badge-host'
      case 'GUEST': return 'badge-guest'
      default: return 'badge-default'
    }
  }

  return (
    <div className="admin-dashboard">
      <div className="admin-header">
        <h1>⚙️ Admin Panel</h1>
        <p className="admin-subtitle">Upravljanje sistemom i korisnicima</p>
      </div>

      <div className="admin-stats">
        <div className="stat-card glass-panel">
          <span className="stat-icon">👥</span>
          <div className="stat-info">
            <span className="stat-value">{totalUsers}</span>
            <span className="stat-label">Ukupno korisnika</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">🏠</span>
          <div className="stat-info">
            <span className="stat-value">{users.filter(u => u.role === 'HOST').length}</span>
            <span className="stat-label">Domaćini (ova str.)</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">🧳</span>
          <div className="stat-info">
            <span className="stat-value">{users.filter(u => u.role === 'GUEST').length}</span>
            <span className="stat-label">Gosti (ova str.)</span>
          </div>
        </div>
      </div>

      {error && <div className="error-alert">{error}</div>}

      <div className="users-table-container glass-panel">
        <h2>Svi korisnici</h2>
        {loading ? (
          <div className="loading">Učitavanje...</div>
        ) : (
          <>
            <table className="users-table" id="admin-users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Ime</th>
                  <th>Email</th>
                  <th>Uloga</th>
                  <th>Telefon</th>
                  <th>Akcije</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>#{u.id}</td>
                    <td>{u.firstName} {u.lastName}</td>
                    <td>{u.email}</td>
                    <td>
                      <span className={`role-badge ${getRoleBadgeClass(u.role)}`}>
                        {u.role}
                      </span>
                    </td>
                    <td>{u.phone || '—'}</td>
                    <td>
                      {deleteConfirm === u.id ? (
                        <div className="delete-confirm">
                          <span>Obrisati?</span>
                          <button onClick={() => handleDelete(u.id)} className="confirm-yes">Da</button>
                          <button onClick={() => setDeleteConfirm(null)} className="confirm-no">Ne</button>
                        </div>
                      ) : (
                        <button
                          onClick={() => setDeleteConfirm(u.id)}
                          className="delete-btn"
                          disabled={u.id === user.id}
                          title={u.id === user.id ? 'Ne možete obrisati sebe' : 'Obriši korisnika'}
                        >
                          🗑️
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="pagination">
              <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>
                Prethodna
              </button>
              <span>Stranica {page + 1} od {totalPages || 1}</span>
              <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1}>
                Sljedeća
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
