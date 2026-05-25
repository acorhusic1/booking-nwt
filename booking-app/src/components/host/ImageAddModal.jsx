import React, { useState, useEffect } from 'react'
import { propertyApi } from '../../api/propertyApi'
import '../../styles/AddPropertyModal.css'

export default function ImageAddModal({ property, isOpen, onClose }) {
  const [images, setImages] = useState([])
  const [url, setUrl] = useState('')
  const [isPrimary, setIsPrimary] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (isOpen && property) {
      fetchImages()
    }
  }, [isOpen, property])

  const fetchImages = async () => {
    try {
      const data = await propertyApi.getImages(property.id)
      setImages(data)
    } catch (err) {
      console.error('Greška pri učitavanju slika:', err)
    }
  }

  if (!isOpen || !property) return null

  const handleAddImage = async (e) => {
    e.preventDefault()
    if (!url) return

    setLoading(true)
    setError(null)

    try {
      const newImage = await propertyApi.addImage(property.id, { url, isPrimary })
      setImages(prev => [...prev, newImage])
      setUrl('')
      setIsPrimary(false)
    } catch (err) {
      console.error('Greška pri dodavanju slike:', err)
      setError(err.response?.data?.message || 'Došlo je do greške prilikom dodavanja slike.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel" style={{ maxWidth: '600px' }}>
        <button className="modal-close" onClick={onClose}>&times;</button>
        <h2>Slike za smještaj: {property.name}</h2>
        
        {error && <div className="error-alert">{error}</div>}

        <div style={{ marginBottom: '20px' }}>
          <h4>Postojeće slike:</h4>
          {images.length === 0 ? (
            <p className="text-muted">Nema dodatih slika.</p>
          ) : (
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginTop: '10px' }}>
              {images.map(img => (
                <div key={img.id} style={{ position: 'relative' }}>
                  <img 
                    src={img.url} 
                    alt="Property" 
                    style={{ width: '100px', height: '100px', objectFit: 'cover', borderRadius: '8px' }} 
                  />
                  {img.isPrimary && (
                    <span style={{ position: 'absolute', top: '5px', left: '5px', background: 'var(--primary-color)', color: 'white', fontSize: '10px', padding: '2px 5px', borderRadius: '4px' }}>
                      Glavna
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        <form onSubmit={handleAddImage} className="add-property-form">
          <div className="form-group">
            <label>URL slike</label>
            <input 
              type="url" 
              value={url} 
              onChange={(e) => setUrl(e.target.value)} 
              required 
              placeholder="https://primjer.com/slika.jpg"
            />
          </div>

          <div className="form-group" style={{ flexDirection: 'row', alignItems: 'center', gap: '10px' }}>
            <input 
              type="checkbox" 
              id="isPrimary"
              checked={isPrimary} 
              onChange={(e) => setIsPrimary(e.target.checked)} 
              style={{ width: 'auto' }}
            />
            <label htmlFor="isPrimary" style={{ cursor: 'pointer' }}>Postavi kao glavnu sliku</label>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Zatvori
            </button>
            <button type="submit" className="btn-primary" disabled={loading || !url}>
              {loading ? 'Dodavanje...' : 'Dodaj sliku'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
