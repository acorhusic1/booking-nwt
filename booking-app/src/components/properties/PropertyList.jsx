import { useState, useEffect } from 'react'
import { propertyApi } from '../../api/propertyApi'
import PropertyCard from './PropertyCard'
import '../../styles/PropertyList.css'

export default function PropertyList({ filters, onPageChange }) {
  const [properties, setProperties] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    const fetchProperties = async () => {
      setLoading(true)
      try {
        const city = filters?.city || ''
        const data = await propertyApi.getAll(page, 10, city)
        setProperties(data.content)
        setTotalPages(Math.ceil(data.totalElements / 10))
        setError(null)
      } catch (err) {
        setError('Greška pri učitavanju smještaja')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    fetchProperties()
  }, [page, filters])

  if (loading) return <div className="loading">Učitavanje smještaja...</div>
  if (error) return <div className="error">{error}</div>

  return (
    <div className="property-list-container">
      <div className="properties-grid">
        {properties.map((property) => (
          <PropertyCard key={property.id} property={property} />
        ))}
      </div>

      {properties.length === 0 && (
        <div className="no-properties">Nema dostupnih smještaja</div>
      )}

      <div className="pagination">
        <button
          onClick={() => setPage(Math.max(0, page - 1))}
          disabled={page === 0}
        >
          Prethodna
        </button>
        <span>Stranica {page + 1} od {totalPages || 1}</span>
        <button
          onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
          disabled={page >= totalPages - 1}
        >
          Sljedeća
        </button>
      </div>
    </div>
  )
}

