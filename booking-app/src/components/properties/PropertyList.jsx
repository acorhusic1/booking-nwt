import { useState, useEffect, useMemo } from 'react'
import { propertyApi } from '../../api/propertyApi'
import PropertyCard from './PropertyCard'
import '../../styles/PropertyList.css'

/**
 * Lista smjestaja sa lokalnim filterom (grad / samo dostupni) i sortiranjem
 * (dostupni prvo). Sve se računa klijentski na osnovu trenutne stranice —
 * SPA princip "fetch jednom, sortiraj/filtraj u memoriji".
 */
export default function PropertyList({ filters }) {
  const [properties, setProperties] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [search, setSearch] = useState(filters?.city || '')
  const [onlyAvailable, setOnlyAvailable] = useState(true)

  useEffect(() => {
    const fetchProperties = async () => {
      setLoading(true)
      try {
        const data = await propertyApi.getAll(page, 10, '')
        setProperties(data.content || [])
        setTotalPages(Math.ceil((data.totalElements || 0) / 10))
        setError(null)
      } catch (err) {
        setError('Greška pri učitavanju smještaja')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    fetchProperties()
  }, [page])

  // Filtriranje + sortiranje u memoriji
  const visible = useMemo(() => {
    let list = [...properties]

    if (search.trim()) {
      const q = search.trim().toLowerCase()
      list = list.filter(p =>
        (p.city || '').toLowerCase().includes(q) ||
        (p.country || '').toLowerCase().includes(q) ||
        (p.name || '').toLowerCase().includes(q)
      )
    }

    if (onlyAvailable) {
      list = list.filter(p => p.available)
    }

    // Sortiraj: prvo dostupni, pa po imenu
    list.sort((a, b) => {
      if (a.available !== b.available) return a.available ? -1 : 1
      return (a.name || '').localeCompare(b.name || '')
    })

    return list
  }, [properties, search, onlyAvailable])

  return (
    <div className="property-list-container">
      <div className="property-filters">
        <input
          type="text"
          placeholder="🔎 Pretraži po gradu, državi ili imenu..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="property-search-input"
        />
        <label className="property-filter-toggle">
          <input
            type="checkbox"
            checked={onlyAvailable}
            onChange={(e) => setOnlyAvailable(e.target.checked)}
          />
          <span>Samo dostupni</span>
        </label>
      </div>

      {loading && <div className="loading">Učitavanje smještaja...</div>}
      {error && <div className="error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="properties-grid">
            {visible.map((property) => (
              <PropertyCard key={property.id} property={property} />
            ))}
          </div>

          {visible.length === 0 && (
            <div className="no-properties">
              {search || onlyAvailable
                ? 'Nema smještaja koji odgovaraju filterima. Pokušaj proširiti pretragu.'
                : 'Nema dostupnih smještaja'}
            </div>
          )}

          <div className="pagination">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
            >
              ← Prethodna
            </button>
            <span>Stranica {page + 1} od {totalPages || 1}</span>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
            >
              Sljedeća →
            </button>
          </div>
        </>
      )}
    </div>
  )
}
