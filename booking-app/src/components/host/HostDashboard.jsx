import { useState, useEffect } from 'react'
import { propertyApi } from '../../api/propertyApi'
import { reservationApi } from '../../api/reservationApi'
import { analyticsApi } from '../../api/analyticsApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate, Link } from 'react-router-dom'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'
import '../../styles/HostDashboard.css'
import AddPropertyModal from './AddPropertyModal'
import ImageAddModal from './ImageAddModal'
import HostVerification from './HostVerification'
import PricingModal from './PricingModal'
import SeasonalRulesModal from './SeasonalRulesModal'
import CalendarModal from './CalendarModal'
import HostProblemReports from './HostProblemReports'
import RevenueChart from './RevenueChart'
import HostAnalyticsPanel from './HostAnalyticsPanel'

export default function HostDashboard() {
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const [properties, setProperties] = useState([])
  const [reservations, setReservations] = useState([])
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [selectedPropertyForImages, setSelectedPropertyForImages] = useState(null)
  const [selectedPropertyForPricing, setSelectedPropertyForPricing] = useState(null)
  const [selectedPropertyForSeasonal, setSelectedPropertyForSeasonal] = useState(null)
  const [selectedPropertyForCalendar, setSelectedPropertyForCalendar] = useState(null)

  const [reports, setReports] = useState([])
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1)
  const [totalRevenueBackend, setTotalRevenueBackend] = useState(0)
  // F11 — filter analitike na jedan smjestaj
  const [selectedPropertyId, setSelectedPropertyId] = useState(null)

  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)

  const mjeseci = ['Januar', 'Februar', 'Mart', 'April', 'Maj', 'Juni', 'Juli', 'August', 'Septembar', 'Oktobar', 'Novembar', 'Decembar']

  useEffect(() => {
    if (user?.role !== 'HOST') {
      navigate('/dashboard')
      return
    }
    fetchData()
  }, [user?.role])

  useEffect(() => {
    if (user?.id) fetchAnalytics()
  }, [user?.id, selectedYear, selectedMonth])

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
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Greška pri učitavanju podataka' })
    } finally {
      setLoading(false)
    }
  }

  const fetchAnalytics = async () => {
    try {
      // Koristimo host-specifican endpoint (hasAnyRole HOST,ADMIN) jer
      // /api/reports/period zahtjeva ADMIN role → 403 za hosta.
      const data = await analyticsApi.getReportsByHostIdAndYear(user.id, selectedYear)
      const hostReports = data.filter(r => r.month === selectedMonth)
      setReports(hostReports)

      const revenue = hostReports.reduce((sum, r) => sum + (r.totalRevenue || 0), 0)
      setTotalRevenueBackend(revenue)
    } catch {
      showToast({ type: 'warning', title: 'Analitika', message: 'Nije moguće učitati analitiku u ovom trenutku.' })
    }
  }

  const handleDeleteProperty = async (id) => {
    if (window.confirm('Da li ste sigurni da želite obrisati ovaj smještaj?')) {
      try {
        await propertyApi.delete(id)
        setProperties(prev => prev.filter(p => p.id !== id))
      } catch (err) {
        console.error('Greška pri brisanju smještaja:', err)
        alert('Došlo je do greške prilikom brisanja smještaja.')
      }
    }
  }

  if (loading) return <Spinner label="Učitavanje domaćin panela..." size="lg" />

  // BUG: "Prihodi" stat — sada se računa iz live rezervacija (matchira chart)
  const liveRevenue = reservations
    .filter(r => ['CONFIRMED', 'ACTIVE', 'COMPLETED'].includes((r.status || '').toUpperCase()))
    .reduce((sum, r) => sum + (Number(r.totalPrice) || 0), 0)

  return (
    <div className="host-dashboard">
      <div className="host-header">
        <h1>🏠 Domaćin Panel</h1>
        <p className="host-subtitle">Dobrodošli, {user.email}!</p>
      </div>

      <HostVerification />

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
            <span className="stat-value">{liveRevenue.toFixed(2)} BAM</span>
            <span className="stat-label">Prihod ukupno</span>
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

      <div className="analytics-controls glass-panel" style={{ padding: '15px', marginBottom: '20px', display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
        <h3 style={{ margin: 0 }}>📊 Analitika</h3>
        <label style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-tertiary)', fontSize: '0.85rem' }}>
          Godina:
          <select value={selectedYear} onChange={e => setSelectedYear(Number(e.target.value))} className="admin-select">
            {[2024, 2025, 2026, 2027].map(y => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </label>
      </div>

      {/* F11 — Stopa popunjenosti, prosjecna ocjena, rank po popularnosti, filter po objektu */}
      <HostAnalyticsPanel
        properties={properties}
        reservations={reservations}
        year={selectedYear}
        selectedPropertyId={selectedPropertyId}
        onSelectProperty={setSelectedPropertyId}
      />

      {/* Bar chart zarade po mjesecu — postuje filter po objektu iz analytics panel-a */}
      <RevenueChart
        reservations={selectedPropertyId
          ? reservations.filter(r => r.propertyId === selectedPropertyId)
          : reservations}
        year={selectedYear}
      />

      <div style={{ height: '20px' }} />

      <div className="host-sections">
        <section className="host-section glass-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h2 style={{ margin: 0 }}>🏘️ Moji smještaji</h2>
            <button className="btn-primary" onClick={() => setIsAddModalOpen(true)}>+ Dodaj smještaj</button>
          </div>
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
                  <div style={{ display: 'flex', gap: '8px', marginTop: '15px', flexWrap: 'wrap' }}>
                    <Link to={`/properties/${prop.id}`} className="view-btn" style={{ flex: '1 1 80px', textAlign: 'center', margin: 0, padding: '8px' }}>Pogledaj</Link>
                    <button className="btn-secondary" onClick={() => setSelectedPropertyForImages(prop)} style={{ flex: '1 1 80px', padding: '8px' }}>Slike</button>
                    <button className="btn-secondary" onClick={() => setSelectedPropertyForPricing(prop)} style={{ flex: '1 1 80px', padding: '8px' }}>💰 Cijene</button>
                    <button className="btn-secondary" onClick={() => setSelectedPropertyForSeasonal(prop)} style={{ flex: '1 1 80px', padding: '8px' }}>🗓 Sezone</button>
                    <button className="btn-secondary" onClick={() => setSelectedPropertyForCalendar(prop)} style={{ flex: '1 1 80px', padding: '8px' }}>📅 Kalendar</button>
                    <button className="btn-secondary" onClick={() => handleDeleteProperty(prop.id)} style={{ flex: '1 1 80px', padding: '8px', color: '#ef4444', borderColor: '#ef4444' }}>Obriši</button>
                  </div>
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

        <HostProblemReports />
      </div>

      <AddPropertyModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onPropertyAdded={(newProp) => setProperties(prev => [...prev, newProp])}
      />

      <ImageAddModal
        property={selectedPropertyForImages}
        isOpen={!!selectedPropertyForImages}
        onClose={() => setSelectedPropertyForImages(null)}
      />

      {selectedPropertyForPricing && (
        <PricingModal
          open={!!selectedPropertyForPricing}
          property={selectedPropertyForPricing}
          onClose={() => setSelectedPropertyForPricing(null)}
        />
      )}

      {selectedPropertyForSeasonal && (
        <SeasonalRulesModal
          open={!!selectedPropertyForSeasonal}
          property={selectedPropertyForSeasonal}
          onClose={() => setSelectedPropertyForSeasonal(null)}
        />
      )}

      {selectedPropertyForCalendar && (
        <CalendarModal
          open={!!selectedPropertyForCalendar}
          property={selectedPropertyForCalendar}
          onClose={() => setSelectedPropertyForCalendar(null)}
        />
      )}
    </div>
  )
}
