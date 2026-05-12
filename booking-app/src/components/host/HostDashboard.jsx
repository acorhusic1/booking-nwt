import { useState, useEffect } from 'react'
import { propertyApi } from '../../api/propertyApi'
import { reservationApi } from '../../api/reservationApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate, Link } from 'react-router-dom'
import '../../styles/HostDashboard.css'

export default function HostDashboard() {
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const [properties, setProperties] = useState([])
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (user?.role !== 'HOST') {
      navigate('/dashboard')
      return
    }
    fetchData()
  }, [user?.role])

  const fetchData = async () => {
    setLoading(true)
    try {
      const [propsData, resData] = await Promise.allSettled([
        propertyApi.getByHostId(user.id),
        reservationApi.getByHostId(user.id)
      ])
      if (propsData.status === 'fulfilled') {
        setProperties(Array.isArray(propsData.value) ? propsData.value : propsData.value?.content || [])
      }
      if (resData.status === 'fulfilled') {
        setReservations(Array.isArray(resData.value) ? resData.value : resData.value?.content || [])
      }
      setError(null)
    } catch (err) {
      setError('Greška pri učitavanju podataka')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const totalRevenue = reservations
    .filter(r => r.status === 'CONFIRMED')
    .reduce((sum, r) => sum + (r.totalPrice || 0), 0)

  if (loading) return <div className="loading">Učitavanje domaćin panela...</div>

  return (
    <div className="host-dashboard">
      <div className="host-header">
        <h1>🏠 Domaćin Panel</h1>
        <p className="host-subtitle">Dobrodošli, {user.email}!</p>
      </div>

      <div className="host-stats">
        <div className="stat-card glass-panel">
          <span className="stat-icon">🏘️</span>
          <div className="stat-info">
            <span className="stat-value">{properties.length}</span>
            <span className="stat-label">Moji smještaji</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">📋</span>
          <div className="stat-info">
            <span className="stat-value">{reservations.length}</span>
            <span className="stat-label">Rezervacije</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">💰</span>
          <div className="stat-info">
            <span className="stat-value">${totalRevenue.toFixed(2)}</span>
            <span className="stat-label">Ukupan prihod</span>
          </div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-icon">✅</span>
          <div className="stat-info">
            <span className="stat-value">
              {reservations.filter(r => r.status === 'CONFIRMED').length}
            </span>
            <span className="stat-label">Potvrđene</span>
          </div>
        </div>
      </div>

      {error && <div className="error-alert">{error}</div>}

      <div className="host-sections">
        <section className="host-section glass-panel">
          <h2>🏘️ Moji smještaji</h2>
          {properties.length === 0 ? (
            <p className="no-data">Nemate registrovanih smještaja</p>
          ) : (
            <div className="host-properties-grid">
              {properties.map((prop) => (
                <div key={prop.id} className="host-property-card">
                  <div className="property-status-bar">
                    <span className={`availability ${prop.available ? 'available' : 'unavailable'}`}>
                      {prop.available ? '✅ Dostupno' : '❌ Zauzeto'}
                    </span>
                  </div>
                  <h3>{prop.name}</h3>
                  <p>📍 {prop.city}</p>
                  <p>💰 ${prop.pricePerNight}/noć</p>
                  <p>👥 Max {prop.maxGuests} osoba</p>
                  <Link to={`/properties/${prop.id}`} className="view-btn">Pogledaj →</Link>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="host-section glass-panel">
          <h2>📋 Rezervacije mojih smještaja</h2>
          {reservations.length === 0 ? (
            <p className="no-data">Nema rezervacija</p>
          ) : (
            <table className="reservations-table" id="host-reservations-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Smještaj</th>
                  <th>Dolazak</th>
                  <th>Odlazak</th>
                  <th>Status</th>
                  <th>Cijena</th>
                </tr>
              </thead>
              <tbody>
                {reservations.map((res) => (
                  <tr key={res.id}>
                    <td>#{res.id}</td>
                    <td>Property #{res.propertyId}</td>
                    <td>{res.checkInDate || res.checkIn}</td>
                    <td>{res.checkOutDate || res.checkOut}</td>
                    <td>
                      <span className={`status-badge status-${(res.status || 'CREATED').toLowerCase()}`}>
                        {res.status || 'CREATED'}
                      </span>
                    </td>
                    <td>${res.totalPrice || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  )
}
