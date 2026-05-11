import { useEffect, useState } from 'react'
import { reservationApi } from '../api/reservationApi'
import { useAuthStore } from '../store/authStore'
import '../styles/Dashboard.css'

export default function Dashboard() {
  const { user, isAuthenticated } = useAuthStore()
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!isAuthenticated || !user?.id) return

    const fetchReservations = async () => {
      try {
        const data = await reservationApi.getByGuestId(user.id)
        // Backend vraca List<ReservationResponseDTO> (ne Page) za /guest/{id}
        setReservations(Array.isArray(data) ? data : (data.content || []))
      } catch (err) {
        setError('Greška pri učitavanju rezervacija')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    fetchReservations()
  }, [isAuthenticated, user?.id])

  if (!isAuthenticated) {
    return <div className="dashboard"><p>Molimo prijavite se</p></div>
  }

  return (
    <div className="dashboard">
      <h1>Dobrodošli, {user.email}!</h1>
      <p>Vaša uloga: <strong>{user.role}</strong></p>

      <section className="reservations-section">
        <h2>Moje Rezervacije</h2>

        {loading && <div className="loading">Učitavanje...</div>}
        {error && <div className="error">{error}</div>}

        {!loading && reservations.length === 0 && (
          <div className="no-data">Nemate rezervacija</div>
        )}

        <div className="reservations-list">
          {reservations.map((res) => (
            <div key={res.id} className="reservation-card">
              <h3>Rezervacija #{res.id}</h3>
              <p>Smještaj ID: {res.propertyId}</p>
              <p>Dolazak: {res.checkIn}</p>
              <p>Odlazak: {res.checkOut}</p>
              <p>Osobe: {res.numGuests}</p>
              <p className="status">Status: <strong>{res.status || 'Aktivna'}</strong></p>
              <p className="price">${res.totalPrice}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
