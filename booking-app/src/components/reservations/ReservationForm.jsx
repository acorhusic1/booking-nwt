import { useForm, useWatch } from 'react-hook-form'
import { useState, useEffect } from 'react'
import { reservationApi } from '../../api/reservationApi'
import { propertyApi } from '../../api/propertyApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../common/ToastProvider'
import '../../styles/ReservationForm.css'

// Privremeno: backend Property model nema cijenu po noći u javnom DTO-u.
// Koristimo flat rate za UI prikaz; final iznos validira backend.
const PRICE_PER_NIGHT_BAM = 100

export default function ReservationForm({ propertyId }) {
  const { register, handleSubmit, control, formState: { errors } } = useForm()
  const { user, isAuthenticated } = useAuthStore()
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [property, setProperty] = useState(null)

  const checkIn = useWatch({ control, name: 'checkIn' })
  const checkOut = useWatch({ control, name: 'checkOut' })

  // BUG-009: minimum date u input-u — sprjecava odabir proslog datuma
  const today = new Date().toISOString().split('T')[0]
  const minCheckOut = checkIn || today

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
      const created = await reservationApi.create(reservationData)
      // BUG-001: replace:true sprjecava da Back dugme vrati korisnika
      // nazad na rezervacionu formu (pa da je submit-uje ponovo)
      navigate(`/dashboard?tab=reservations&pendingId=${created.id}&pending=1`, { replace: true })
      await reservationApi.create(reservationData)
      navigate('/dashboard?tab=notifications&pending=1')
    } catch (err) {
      const msg = err.response?.data?.message
      const text = typeof msg === 'string' ? msg : 'Greška pri kreiranju rezervacije'
      setError(text)
      showToast({ type: 'error', title: 'Rezervacija nije kreirana', message: text, duration: 7000 })
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
          min={today}
        />
        {errors.checkIn && <span className="error">{errors.checkIn.message}</span>}
      </div>

      <div className="form-group">
        <label>Datum odlaska:</label>
        <input
          {...register('checkOut', { required: 'Obavezno polje' })}
          type="date"
          min={minCheckOut}
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
