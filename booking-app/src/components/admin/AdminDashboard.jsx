import { useState, useEffect } from 'react'
import { userApi } from '../../api/userApi'
import { analyticsApi } from '../../api/analyticsApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import Spinner from '../common/Spinner'
import '../../styles/AdminDashboard.css'

export default function AdminDashboard() {
  const { user } = useAuthStore()
  const navigate = useNavigate()
  
  // Tabs
  const [activeTab, setActiveTab] = useState('users') // 'users' or 'analytics'

  // Users State
  const [users, setUsers] = useState([])
  const [loadingUsers, setLoadingUsers] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalUsers, setTotalUsers] = useState(0)
  const [deleteConfirm, setDeleteConfirm] = useState(null)
  const [searchRole, setSearchRole] = useState('')

  // Analytics State
  const [stats, setStats] = useState([])
  const [loadingStats, setLoadingStats] = useState(false)
  const [statsPage, setStatsPage] = useState(0)
  const [statsTotalPages, setStatsTotalPages] = useState(0)

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      navigate('/dashboard')
      return
    }
  }, [user?.role])

  useEffect(() => {
    if (activeTab === 'users') {
      fetchUsers()
    } else if (activeTab === 'analytics') {
      fetchStats()
    }
  }, [page, searchRole, activeTab, statsPage])

  const fetchUsers = async () => {
    setLoadingUsers(true)
    try {
      let data
      if (searchRole) {
        data = await userApi.search(searchRole, undefined, page, 10)
      } else {
        data = await userApi.getAll(page, 10)
      }
      setUsers(data.content || [])
      setTotalPages(data.totalPages || 1)
      setTotalUsers(data.totalElements || 0)
      setError(null)
    } catch {
      setError('Greška pri učitavanju korisnika')
    } finally {
      setLoadingUsers(false)
    }
  }

  const fetchStats = async () => {
    setLoadingStats(true)
    try {
      const data = await analyticsApi.getAllStatisticsPaginated(statsPage, 10)
      setStats(data.content || [])
      setStatsTotalPages(data.totalPages || 1)
      setError(null)
    } catch {
      setError('Greška pri učitavanju sistemske analitike')
    } finally {
      setLoadingStats(false)
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

      <div className="admin-tabs" style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button 
          onClick={() => setActiveTab('users')} 
          className={activeTab === 'users' ? 'tab-btn active' : 'tab-btn'}
          style={{ padding: '10px 20px', cursor: 'pointer', borderRadius: '5px', border: '1px solid #ccc', background: activeTab === 'users' ? '#4f46e5' : '#fff', color: activeTab === 'users' ? '#fff' : '#333' }}
        >
          Korisnici
        </button>
        <button 
          onClick={() => setActiveTab('analytics')} 
          className={activeTab === 'analytics' ? 'tab-btn active' : 'tab-btn'}
          style={{ padding: '10px 20px', cursor: 'pointer', borderRadius: '5px', border: '1px solid #ccc', background: activeTab === 'analytics' ? '#4f46e5' : '#fff', color: activeTab === 'analytics' ? '#fff' : '#333' }}
        >
          Sistemska Analitika
        </button>
      </div>

      {error && <div className="error-alert">{error}</div>}

      {activeTab === 'users' && (
        <>
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

          <div className="users-table-container glass-panel">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
              <h2>Svi korisnici</h2>
              <div className="user-filters">
                <select value={searchRole} onChange={e => { setSearchRole(e.target.value); setPage(0); }} style={{ padding: '8px', borderRadius: '5px' }}>
                  <option value="">Sve uloge</option>
                  <option value="ADMIN">Admini</option>
                  <option value="HOST">Domaćini</option>
                  <option value="GUEST">Gosti</option>
                </select>
              </div>
            </div>
            
            {loadingUsers ? (
              <Spinner />
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
        </>
      )}

      {activeTab === 'analytics' && (
        <div className="analytics-table-container glass-panel">
          <h2>Platformska Statistika</h2>
          {loadingStats ? (
            <Spinner label="Učitavanje analitike..." />
          ) : (
            <>
              <table className="users-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Smještaj ID</th>
                    <th>Period (G/M)</th>
                    <th>Zarada</th>
                    <th>Broj rezervacija</th>
                    <th>Stopa popunjenosti</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.length === 0 ? (
                    <tr><td colSpan="6" style={{textAlign: 'center'}}>Nema podataka</td></tr>
                  ) : (
                    stats.map((s) => (
                      <tr key={s.id}>
                        <td>#{s.id}</td>
                        <td>{s.propertyId}</td>
                        <td>{s.year}/{s.month}</td>
                        <td>${(s.totalRevenue || 0).toFixed(2)}</td>
                        <td>{s.totalReservations || 0}</td>
                        <td>{(s.occupancyRate || 0).toFixed(1)}%</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>

              <div className="pagination">
                <button onClick={() => setStatsPage(Math.max(0, statsPage - 1))} disabled={statsPage === 0}>
                  Prethodna
                </button>
                <span>Stranica {statsPage + 1} od {statsTotalPages || 1}</span>
                <button onClick={() => setStatsPage(Math.min(statsTotalPages - 1, statsPage + 1))} disabled={statsPage >= statsTotalPages - 1}>
                  Sljedeća
                </button>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  )
}
