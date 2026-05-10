import { useForm } from 'react-hook-form'
import { useState } from 'react'
import { reservationApi } from '../../api/reservationApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import '../../styles/ReservationForm.css'

export default function ReservationForm({ propertyId }) {
  const { register, handleSubmit, formState: { errors } } = useForm()
  const { user, isAuthenticated } = useAuthStore()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const onSubmit = async (data) => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }

    setLoading(true)
    setError(null)
    try {
      const reservationData = {
        propertyId: Number(propertyId),
        userId: user.id,
        checkInDate: data.checkInDate,
        checkOutDate: data.checkOutDate,
        numberOfGuests: Number(data.numberOfGuests),
        totalPrice: data.totalPrice
      }
      await reservationApi.create(reservationData)
      alert('Rezervacija uspješna!')
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri kreiranju rezervacije')
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
          {...register('checkInDate', { required: 'Obavezno polje' })}
          type="date"
        />
        {errors.checkInDate && <span className="error">{errors.checkInDate.message}</span>}
      </div>

      <div className="form-group">
        <label>Datum odlaska:</label>
        <input
          {...register('checkOutDate', { required: 'Obavezno polje' })}
          type="date"
        />
        {errors.checkOutDate && <span className="error">{errors.checkOutDate.message}</span>}
      </div>

      <div className="form-group">
        <label>Broj osoba:</label>
        <input
          {...register('numberOfGuests', { required: 'Obavezno polje' })}
          type="number"
          min="1"
        />
        {errors.numberOfGuests && <span className="error">{errors.numberOfGuests.message}</span>}
      </div>

      <div className="form-group">
        <label>Ukupna cijena:</label>
        <input
          {...register('totalPrice', { required: 'Obavezno polje' })}
          type="number"
          step="0.01"
          min="0"
        />
        {errors.totalPrice && <span className="error">{errors.totalPrice.message}</span>}
      </div>

      {error && <div className="error-alert">{error}</div>}

      <button type="submit" disabled={loading} className="submit-btn">
        {loading ? 'Rezerviranje...' : 'Potvrdi rezervaciju'}
      </button>
    </form>
  )
}

