import { useState, useEffect } from 'react'
import { userApi } from '../../api/userApi'
import { reservationApi } from '../../api/reservationApi'
import { propertyApi } from '../../api/propertyApi'
import { verificationApi } from '../../api/verificationApi'
import { problemReportApi } from '../../api/problemReportApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'
import AdminVerifications from './AdminVerifications'
import AdminPropertyModeration from './AdminPropertyModeration'
import AdminProblemReports from './AdminProblemReports'
import '../../styles/AdminDashboard.css'

export default function AdminDashboard() {
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const { showToast } = useToast()
  
  // Tabs
  const [activeTab, setActiveTab] = useState('users') // 'users' or 'analytics'

  // Users State
  const [users, setUsers] = useState([])
  const [loadingUsers, setLoadingUsers] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalUsers, setTotalUsers] = useState(0)
  const [deleteConfirm, setDeleteConfirm] = useState(null)
  const [searchRole, setSearchRole] = useState('')

  // Pregled platforme — STVARNI podaci izracunati iz zivih servisa
  // (raniji tab je prikazivao rucno seedovane redove iz analytics-service-a
  // pa su brojke poput "popunjenost 50%" izgledale izmisljeno — i bile su)
  const [overview, setOverview] = useState(null)
  const [loadingStats, setLoadingStats] = useState(false)

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
      fetchOverview()
    }
  }, [page, searchRole, activeTab])

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
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Greška pri učitavanju korisnika' })
    } finally {
      setLoadingUsers(false)
    }
  }

  const fetchOverview = async () => {
    setLoadingStats(true)
    try {
      const [resR, propR, pendR, verifR, repR, usersR] = await Promise.allSettled([
        reservationApi.getAll(),                  // ADMIN — sve rezervacije
        propertyApi.getAll(0, 1000, ''),          // javni (APPROVED) objekti
        propertyApi.getPendingModeration(),       // ADMIN — na cekanju moderacije
        verificationApi.getAll(),                 // ADMIN — verifikacije
        problemReportApi.getAll(),                // ADMIN — prijave problema
        userApi.getAll(0, 1)                      // samo radi totalElements
      ])
      const val = (r, fb) => (r.status === 'fulfilled' ? r.value : fb)
      const reservations = (() => {
        const v = val(resR, [])
        return Array.isArray(v) ? v : (v.content || [])
      })()
      const properties = val(propR, { content: [] }).content || []
      const pendingProps = val(pendR, [])
      const verifications = val(verifR, [])
      const reports = val(repR, [])
      const totalUsersCount = val(usersR, {}).totalElements || 0

      // Rezervacije po statusu
      const byStatus = {}
      reservations.forEach(r => {
        const s = (r.status || 'CREATED').toUpperCase()
        byStatus[s] = (byStatus[s] || 0) + 1
      })

      // Promet = naplacene rezervacije; provizija platforme 10%
      const paidStatuses = ['CONFIRMED', 'ACTIVE', 'COMPLETED']
      const revenue = reservations
        .filter(r => paidStatuses.includes((r.status || '').toUpperCase()))
        .reduce((s, r) => s + Number(r.totalPrice || 0), 0)

      // Popunjenost TEKUCE godine — bukirane noci se SIJEKU na granice godine
      // (puna duzina boravka van godine je ranije naduvavala procenat)
      const year = new Date().getFullYear()
      const yearStart = new Date(year, 0, 1)
      const yearEnd = new Date(year + 1, 0, 1)
      const bookedNights = reservations
        .filter(r => paidStatuses.includes((r.status || '').toUpperCase()) && r.checkIn && r.checkOut)
        .reduce((sum, r) => {
          const from = new Date(Math.max(new Date(r.checkIn), yearStart))
          const to = new Date(Math.min(new Date(r.checkOut), yearEnd))
          return sum + Math.max(0, Math.round((to - from) / 86400000))
        }, 0)
      const daysInYear = ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0) ? 366 : 365
      const occupancy = properties.length > 0
        ? Math.min(100, (bookedNights / (daysInYear * properties.length)) * 100)
        : 0

      setOverview({
        totalUsersCount,
        propertiesCount: properties.length,
        pendingModeration: pendingProps.length,
        reservationsCount: reservations.length,
        byStatus,
        revenue,
        commission: revenue * 0.10,
        occupancy,
        bookedNights,
        pendingVerifications: verifications.filter(v => (v.status || '').toUpperCase() === 'PENDING').length,
        openReports: reports.filter(r => ['REPORTED', 'IN_PROGRESS'].includes((r.status || '').toUpperCase())).length
      })
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Greška pri učitavanju pregleda platforme' })
    } finally {
      setLoadingStats(false)
    }
  }

  const handleDelete = async (userId) => {
    try {
      await userApi.delete(userId)
      setDeleteConfirm(null)
      showToast({ type: 'success', title: 'Obrisano', message: 'Korisnik je uspješno obrisan.' })
      fetchUsers()
    } catch (err) {
      showToast({ type: 'error', title: 'Greška', message: 'Greška pri brisanju korisnika' })
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

      <div className="admin-tabs">
        <button
          onClick={() => setActiveTab('users')}
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
        >
          👥 Korisnici
        </button>
        <button
          onClick={() => setActiveTab('analytics')}
          className={`tab-btn ${activeTab === 'analytics' ? 'active' : ''}`}
        >
          📊 Pregled platforme
        </button>
        <button
          onClick={() => setActiveTab('verifications')}
          className={`tab-btn ${activeTab === 'verifications' ? 'active' : ''}`}
        >
          🪪 Verifikacije
        </button>
        <button
          onClick={() => setActiveTab('reports')}
          className={`tab-btn ${activeTab === 'reports' ? 'active' : ''}`}
        >
          ⚠ Prijave problema
        </button>
        <button
          onClick={() => setActiveTab('moderation')}
          className={`tab-btn ${activeTab === 'moderation' ? 'active' : ''}`}
        >
          🛡 Moderacija smještaja
        </button>
      </div>

      {activeTab === 'verifications' && <AdminVerifications />}
      {activeTab === 'reports' && <AdminProblemReports />}
      {activeTab === 'moderation' && <AdminPropertyModeration />}

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
                <select value={searchRole} onChange={e => { setSearchRole(e.target.value); setPage(0); }} className="admin-select">
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
          <h2>Pregled platforme</h2>
          <p style={{ color: 'var(--text-tertiary)', fontSize: '0.85rem', marginBottom: '16px' }}>
            Podaci izračunati uživo iz rezervacija, smještaja, verifikacija i prijava.
          </p>
          {loadingStats && <Spinner label="Učitavanje pregleda..." />}
          {!loadingStats && overview && (
            <>
              <div className="admin-stats" style={{ marginBottom: '20px' }}>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">👥</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.totalUsersCount}</span>
                    <span className="stat-label">Korisnika</span>
                  </div>
                </div>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">🏠</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.propertiesCount}</span>
                    <span className="stat-label">Objavljenih smještaja</span>
                  </div>
                </div>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">📅</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.reservationsCount}</span>
                    <span className="stat-label">Ukupno rezervacija</span>
                  </div>
                </div>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">💰</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.revenue.toFixed(0)} BAM</span>
                    <span className="stat-label">Promet (provizija {overview.commission.toFixed(0)} BAM)</span>
                  </div>
                </div>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">📊</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.occupancy.toFixed(1)}%</span>
                    <span className="stat-label">Popunjenost {new Date().getFullYear()} ({overview.bookedNights} noći)</span>
                  </div>
                </div>
              </div>

              <div className="admin-stats" style={{ marginBottom: '20px' }}>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">🛡</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.pendingModeration}</span>
                    <span className="stat-label">Čeka moderaciju</span>
                  </div>
                </div>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">🪪</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.pendingVerifications}</span>
                    <span className="stat-label">Verifikacije na čekanju</span>
                  </div>
                </div>
                <div className="stat-card glass-panel">
                  <span className="stat-icon">⚠</span>
                  <div className="stat-info">
                    <span className="stat-value">{overview.openReports}</span>
                    <span className="stat-label">Otvorene prijave problema</span>
                  </div>
                </div>
              </div>

              <h3 style={{ marginBottom: '10px' }}>Rezervacije po statusu</h3>
              <table className="users-table">
                <thead>
                  <tr>
                    <th>Status</th>
                    <th>Broj</th>
                    <th>Udio</th>
                  </tr>
                </thead>
                <tbody>
                  {['CREATED', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED'].map(s => (
                    <tr key={s}>
                      <td>{s}</td>
                      <td>{overview.byStatus[s] || 0}</td>
                      <td>
                        {overview.reservationsCount > 0
                          ? (((overview.byStatus[s] || 0) / overview.reservationsCount) * 100).toFixed(0)
                          : 0}%
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </div>
      )}
    </div>
  )
}
