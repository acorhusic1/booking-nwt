import { useForm, useWatch } from 'react-hook-form'
import { useState, useEffect } from 'react'
import { reservationApi } from '../../api/reservationApi'
import { propertyApi } from '../../api/propertyApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import '../../styles/ReservationForm.css'

// Privremeno: backend Property model nema cijenu po noći u javnom DTO-u.
// Koristimo flat rate za UI prikaz; final iznos validira backend.
const PRICE_PER_NIGHT_BAM = 100

export default function ReservationForm({ propertyId }) {
  const { register, handleSubmit, control, formState: { errors } } = useForm()
  const { user, isAuthenticated } = useAuthStore()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [property, setProperty] = useState(null)

  const checkIn = useWatch({ control, name: 'checkIn' })
  const checkOut = useWatch({ control, name: 'checkOut' })

  // hostId trebamo backendu — uzimamo ga iz property-a
  useEffect(() => {
    if (!propertyId) return
    propertyApi.getById(propertyId)
      .then(setProperty)
      .catch(() => setError('Greška pri učitavanju smještaja'))
  }, [propertyId])

  const nights = (() => {
    if (!checkIn || !checkOut) return 0
    const start = new Date(checkIn)
    const end = new Date(checkOut)
    const diff = Math.round((end - start) / (1000 * 60 * 60 * 24))
    return diff > 0 ? diff : 0
  })()

  const totalPrice = nights * PRICE_PER_NIGHT_BAM

  const onSubmit = async (data) => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    if (!property) {
      setError('Smještaj još nije učitan')
      return
    }
    if (nights <= 0) {
      setError('Datum odlaska mora biti nakon datuma dolaska')
      return
    }

    setLoading(true)
    setError(null)
    try {
      const reservationData = {
        guestId: user.id,
        hostId: property.hostId,
        propertyId: Number(propertyId),
        checkIn: data.checkIn,
        checkOut: data.checkOut,
        numGuests: Number(data.numGuests),
        totalPrice: totalPrice
      }
      await reservationApi.create(reservationData)
      alert('Rezervacija uspješna! Plaćanje se obrađuje u pozadini (Saga).')
      navigate('/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message
      setError(typeof msg === 'string' ? msg : 'Greška pri kreiranju rezervacije')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="reservation-form">
      <h3>Rezerviši smještaj</h3>

      <div className="form-group">
        <label>Datum dolaska:</label>
        <input
          {...register('checkIn', { required: 'Obavezno polje' })}
          type="date"
        />
        {errors.checkIn && <span className="error">{errors.checkIn.message}</span>}
      </div>

      <div className="form-group">
        <label>Datum odlaska:</label>
        <input
          {...register('checkOut', { required: 'Obavezno polje' })}
          type="date"
        />
        {errors.checkOut && <span className="error">{errors.checkOut.message}</span>}
      </div>

      <div className="form-group">
        <label>Broj osoba:</label>
        <input
          {...register('numGuests', { required: 'Obavezno polje', min: 1 })}
          type="number"
          min="1"
          defaultValue={1}
        />
        {errors.numGuests && <span className="error">{errors.numGuests.message}</span>}
      </div>

      <div className="summary">
        <span>{nights > 0 ? `${nights} noći × ${PRICE_PER_NIGHT_BAM} BAM` : 'Odaberi datume'}</span>
        <span>Ukupno: <strong>{totalPrice} BAM</strong></span>
      </div>

      {error && <div className="error-alert">{error}</div>}

      <button type="submit" disabled={loading || !property || nights <= 0} className="submit-btn">
        {loading ? 'Rezerviranje...' : 'Potvrdi rezervaciju'}
      </button>
    </form>
  )
}
