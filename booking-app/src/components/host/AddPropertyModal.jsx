import React, { useState } from 'react'
import { propertyApi } from '../../api/propertyApi'
import { useAuthStore } from '../../store/authStore'
import '../../styles/AddPropertyModal.css'

export default function AddPropertyModal({ isOpen, onClose, onPropertyAdded }) {
  const { user } = useAuthStore()
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    address: '',
    city: '',
    country: '',
    maxGuests: 1
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  if (!isOpen) return null

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: name === 'maxGuests' ? parseInt(value) || 1 : value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const propertyData = {
        ...formData,
        hostId: user?.id
      }
      const newProperty = await propertyApi.create(propertyData)
      onPropertyAdded(newProperty)
      onClose()
      setFormData({
        name: '',
        description: '',
        address: '',
        city: '',
        country: '',
        maxGuests: 1
      })
    } catch (err) {
      console.error('Greška pri dodavanju smještaja:', err)
      // Pokušaj izvući specifičnu grešku validacije iz response-a
      const responseData = err.response?.data
      if (responseData) {
        if (typeof responseData === 'string') {
          setError(responseData)
        } else if (responseData.message) {
          // Validation errors: message može biti { field: "error" } Map ili string
          if (typeof responseData.message === 'object') {
            const fieldErrors = Object.entries(responseData.message)
              .map(([field, msg]) => `${field}: ${msg}`)
              .join(' | ')
            setError(fieldErrors)
          } else {
            setError(responseData.message)
          }
        } else {
          setError('Došlo je do greške: ' + (responseData.error || 'Nepoznata greška'))
        }
      } else {
        setError('Došlo je do greške prilikom dodavanja smještaja.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-content">
        <button className="modal-close" onClick={onClose} title="Zatvori">&times;</button>
        <h2>➕ Dodaj novi smještaj</h2>
        <p className="modal-subtitle">Popunite informacije o vašem smještaju</p>

        <form onSubmit={handleSubmit} className="add-property-form">

          <div className="form-section-label">Osnovne informacije</div>

          <div className="form-group">
            <label>Naziv smještaja</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="Npr. Apartman Sunshine"
            />
          </div>

          <div className="form-group">
            <label>Opis</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="3"
              placeholder="Opišite vaš smještaj, pogodnosti, lokaciju..."
            />
          </div>

          <div className="form-section-label">Lokacija</div>

          <div className="form-group">
            <label>Adresa</label>
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={handleChange}
              required
              placeholder="Npr. Titova 15"
            />
          </div>

          <div className="form-row">
            <div className="form-group half">
              <label>Grad</label>
              <input
                type="text"
                name="city"
                value={formData.city}
                onChange={handleChange}
                required
                placeholder="Npr. Sarajevo"
              />
            </div>
            <div className="form-group half">
              <label>Država</label>
              <input
                type="text"
                name="country"
                value={formData.country}
                onChange={handleChange}
                required
                placeholder="Npr. Bosna i Hercegovina"
              />
            </div>
          </div>

          <div className="form-section-label">Kapacitet</div>

          <div className="form-group">
            <label>Maksimalan broj gostiju</label>
            <input
              type="number"
              name="maxGuests"
              value={formData.maxGuests}
              onChange={handleChange}
              min="1"
              max="50"
              required
            />
          </div>

          {error && <div className="error-alert">{error}</div>}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={loading}>
              Odustani
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? '⏳ Dodavanje...' : '✓ Dodaj smještaj'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
