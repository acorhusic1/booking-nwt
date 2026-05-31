import { useState, useEffect, useMemo, useCallback } from 'react'
import { propertyApi } from '../../api/propertyApi'
import { reservationApi } from '../../api/reservationApi'
import { reviewApi } from '../../api/reviewApi'
import { wishlistApi } from '../../api/wishlistApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import PropertyCard from './PropertyCard'
import Spinner from '../common/Spinner'
import ErrorState from '../common/ErrorState'
import '../../styles/PropertyList.css'

/**
 * Lista smjestaja sa lokalnim filterom (grad / samo dostupni) i sortiranjem
 * (dostupni prvo). Sve se računa klijentski na osnovu trenutne stranice —
 * SPA princip "fetch jednom, sortiraj/filtraj u memoriji".
 */
export default function PropertyList({ filters }) {
  const { user, isAuthenticated } = useAuthStore()
  const { showToast } = useToast()
  const isGuest = (user?.role || '').toUpperCase() === 'GUEST'

  const [properties, setProperties] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [search, setSearch] = useState(filters?.city || '')
  const [onlyAvailable, setOnlyAvailable] = useState(true)
  // F1 — prosireni filteri
  const [country, setCountry] = useState('')
  const [minGuests, setMinGuests] = useState(1)
  const [sortBy, setSortBy] = useState('default')
  const [showFilters, setShowFilters] = useState(false)
  // F1 — period boravka (koristi backend /search endpoint kad su oba datuma data)
  const [checkIn, setCheckIn] = useState('')
  const [checkOut, setCheckOut] = useState('')
  // F1 — cijena range + min ocjena + amenities checkboxes
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [minRating, setMinRating] = useState(0)
  const [amenityFilters, setAmenityFilters] = useState({
    WIFI: false, PARKING: false, AC: false, POOL: false
  })
  // F1 — prosjecne ocjene po property-u (dohvacene jednom za sve vidljive)
  const [ratingByProp, setRatingByProp] = useState({})

  // F10 — wishlist state: mapa propertyId → wishlistItemId (da znamo i ukloniti)
  const [wishlistItems, setWishlistItems] = useState({}) // { propertyId: itemId }
  const [defaultListId, setDefaultListId] = useState(null)

  const fetchProperties = async () => {
    setLoading(true)
    try {
      // F1 — ako su check-in/check-out unijeti, koristi backend /search za "dostupno u periodu"
      if (checkIn && checkOut && (search.trim() || country.trim())) {
        const cityForSearch = search.trim() || country.trim()
        const data = await propertyApi.search(cityForSearch, checkIn, checkOut)
        const raw = Array.isArray(data) ? data : []
        // BUG D — property-service /search proverava samo CalendarBlock (host blokovi),
        // NE i stvarne rezervacije. Zato za svaki rezultat dohvatimo occupied-dates
        // i izbacimo property ako mu se neka rezervacija preklapa s [checkIn, checkOut).
        const reqStart = new Date(checkIn)
        const reqEnd = new Date(checkOut)
        const checked = await Promise.all(raw.map(async (p) => {
          try {
            const occ = await reservationApi.getOccupiedDates(p.id)
            const clash = (occ || []).some(o => {
              const s = new Date(o.checkIn)
              const e = new Date(o.checkOut)
              return s < reqEnd && e > reqStart
            })
            return clash ? null : p
          } catch {
            return p
          }
        }))
        setProperties(checked.filter(Boolean))
        setTotalPages(1)
      } else {
        const data = await propertyApi.getAll(page, 10, '')
        setProperties(data.content || [])
        setTotalPages(Math.ceil((data.totalElements || 0) / 10))
      }
      setError(null)
    } catch {
      setError('Greška pri učitavanju smještaja')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchProperties() }, [page])

  // F1 — dropdown drzava izveden iz ucitanih property-a.
  // Ukljucujemo i trenutno odabranu zemlju cak i ako trenutni rezultati
  // ne sadrze nista iz nje — inace dropdown "izgubi" tu opciju nakon search-a
  // i select izgleda prazan iako je state postavljen.
  const availableCountries = useMemo(() => {
    const set = new Set(properties.map(p => p.country).filter(Boolean))
    if (country) set.add(country)
    return Array.from(set).sort()
  }, [properties, country])

  // F1 — dohvati prosjecne ocjene za vidljive propertije (za rating filter + sort)
  useEffect(() => {
    if (properties.length === 0) return
    let cancelled = false
    Promise.all(
      properties.map(p =>
        reviewApi.getByProperty(p.id)
          .then(rs => {
            const arr = Array.isArray(rs) ? rs : []
            if (arr.length === 0) return [p.id, 0]
            const avg = arr.reduce((s, r) => s + Number(r.overallRating || 0), 0) / arr.length
            return [p.id, avg]
          })
          .catch(() => [p.id, 0])
      )
    ).then(pairs => {
      if (cancelled) return
      const m = {}
      pairs.forEach(([id, avg]) => { m[id] = avg })
      setRatingByProp(m)
    })
    return () => { cancelled = true }
  }, [properties.map(p => p.id).join(',')])

  // F10 — dohvati gostovu prvu (default) wishlist listu + stavke
  const loadWishlist = useCallback(async () => {
    if (!isAuthenticated || !isGuest || !user?.id) return
    try {
      const lists = await wishlistApi.getByGuest(user.id)
      if (lists.length === 0) return // nema liste — kreirat ce se lazy pri prvom srcu
      const def = lists[0]
      setDefaultListId(def.id)
      const items = await wishlistApi.getItems(def.id)
      const map = {}
      items.forEach((it) => { map[it.propertyId] = it.id })
      setWishlistItems(map)
    } catch {
      // tiho — wishlist nije kriticna funkcija za prikaz liste
    }
  }, [isAuthenticated, isGuest, user?.id])

  useEffect(() => { loadWishlist() }, [loadWishlist])

  const toggleWishlist = async (propertyId) => {
    if (!isAuthenticated || !isGuest) {
      showToast({ type: 'info', title: 'Prijava potrebna', message: 'Prijavite se kao gost da sačuvate smještaj.' })
      return
    }
    try {
      let listId = defaultListId
      // Lazy kreiranje default liste pri prvom srcu
      if (!listId) {
        const created = await wishlistApi.create(user.id, 'Moja lista')
        listId = created.id
        setDefaultListId(listId)
      }

      const existingItemId = wishlistItems[propertyId]
      if (existingItemId) {
        await wishlistApi.removeItem(listId, existingItemId)
        setWishlistItems((prev) => {
          const next = { ...prev }
          delete next[propertyId]
          return next
        })
      } else {
        const item = await wishlistApi.addItem(listId, propertyId)
        setWishlistItems((prev) => ({ ...prev, [propertyId]: item.id }))
        showToast({ type: 'success', title: 'Sačuvano', message: 'Smještaj dodan u listu želja.' })
      }
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Lista želja nije ažurirana.' })
    }
  }

  // F1 — kombinovano filtriranje + sortiranje u memoriji
  const visible = useMemo(() => {
    let list = [...properties]

    if (search.trim()) {
      const q = search.trim().toLowerCase()
      list = list.filter(p =>
        (p.city || '').toLowerCase().includes(q) ||
        (p.country || '').toLowerCase().includes(q) ||
        (p.name || '').toLowerCase().includes(q) ||
        (p.address || '').toLowerCase().includes(q)
      )
    }

    // Kad je aktivan date-period search, backend je vec primijenio LIKE filter
    // po city ILI country sa istim terminom. Dodatni strict === filter na
    // frontendu je suvisak i moze izbaciti validne stavke (npr. backend match
    // bio po city dok je country drugog tipa).
    const dateSearchActive = !!(checkIn && checkOut && (search.trim() || country.trim()))
    if (country && !dateSearchActive) {
      list = list.filter(p => p.country === country)
    }

    if (minGuests > 1) {
      list = list.filter(p => (p.maxGuests || 0) >= minGuests)
    }

    if (onlyAvailable) {
      list = list.filter(p => p.available)
    }

    // F1 — cijena range filter
    const minP = Number(minPrice) || 0
    const maxP = Number(maxPrice) || Infinity
    if (minP > 0 || maxP < Infinity) {
      list = list.filter(p => {
        const price = Number((p.basePrice ?? p.pricePerNight)) || 0
        return price >= minP && price <= maxP
      })
    }

    // F1 — min ocjena (na osnovu reviews fetch-a)
    if (minRating > 0) {
      list = list.filter(p => (ratingByProp[p.id] || 0) >= minRating)
    }

    // F1 — amenities filter (smjestaj mora imati SVE oznacene)
    const activeAmenities = Object.entries(amenityFilters).filter(([, v]) => v).map(([k]) => k)
    if (activeAmenities.length > 0) {
      list = list.filter(p => {
        const names = (p.amenities || []).map(a => (typeof a === 'string' ? a : a.name || '').toUpperCase())
        return activeAmenities.every(a => names.includes(a))
      })
    }

    // Sortiranje
    switch (sortBy) {
      case 'name':
        list.sort((a, b) => (a.name || '').localeCompare(b.name || ''))
        break
      case 'capacity':
        list.sort((a, b) => (b.maxGuests || 0) - (a.maxGuests || 0))
        break
      case 'city':
        list.sort((a, b) => (a.city || '').localeCompare(b.city || ''))
        break
      case 'price-asc':
        list.sort((a, b) => (Number(a.pricePerNight) || 0) - (Number(b.pricePerNight) || 0))
        break
      case 'price-desc':
        list.sort((a, b) => (Number(b.pricePerNight) || 0) - (Number(a.pricePerNight) || 0))
        break
      case 'rating-desc':
        list.sort((a, b) => (ratingByProp[b.id] || 0) - (ratingByProp[a.id] || 0))
        break
      default: // dostupni prvo, pa po imenu
        list.sort((a, b) => {
          if (a.available !== b.available) return a.available ? -1 : 1
          return (a.name || '').localeCompare(b.name || '')
        })
    }

    return list
  }, [properties, search, country, minGuests, onlyAvailable, sortBy, minPrice, maxPrice, minRating, amenityFilters, ratingByProp])

  const resetFilters = () => {
    setSearch(''); setCountry(''); setMinGuests(1)
    setOnlyAvailable(true); setSortBy('default')
    setCheckIn(''); setCheckOut('')
    setMinPrice(''); setMaxPrice(''); setMinRating(0)
    setAmenityFilters({ WIFI: false, PARKING: false, AC: false, POOL: false })
  }

  const handleDateSearch = (e) => {
    e.preventDefault()
    fetchProperties()
  }

  const today = new Date().toISOString().split('T')[0]

  return (
    <div className="property-list-container">
      <div className="property-filters">
        <input
          type="text"
          placeholder="🔎 Pretraži po gradu, državi, adresi ili imenu..."
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
        <button
          type="button"
          className="filter-toggle-btn"
          onClick={() => setShowFilters(s => !s)}
        >
          {showFilters ? '▲ Sakrij filtere' : '▼ Više filtera'}
        </button>
      </div>

      {showFilters && (
        <div className="property-advanced-filters">
          <div className="filter-row">
            <label>Država:
              <select value={country} onChange={(e) => setCountry(e.target.value)}>
                <option value="">Sve države</option>
                {availableCountries.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </label>

            <label>Min. broj osoba:
              <input
                type="number"
                min="1"
                max="20"
                value={minGuests}
                onChange={(e) => setMinGuests(Math.max(1, Number(e.target.value) || 1))}
              />
            </label>

            <label>Sortiraj po:
              <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                <option value="default">Dostupni prvo</option>
                <option value="name">Imenu (A-Z)</option>
                <option value="capacity">Kapacitetu (najveci prvi)</option>
                <option value="city">Gradu (A-Z)</option>
                <option value="price-asc">Cijeni ↑ (jeftiniji prvi)</option>
                <option value="price-desc">Cijeni ↓ (skuplji prvi)</option>
                <option value="rating-desc">Ocjeni ↓ (najbolji prvi)</option>
              </select>
            </label>
          </div>

          {/* F1 — cijena range + min ocjena */}
          <div className="filter-row">
            <label>Min. cijena/noć:
              <input type="number" min="0" placeholder="0" value={minPrice}
                     onChange={(e) => setMinPrice(e.target.value)} style={{ width: '90px' }} />
            </label>
            <label>Max. cijena/noć:
              <input type="number" min="0" placeholder="∞" value={maxPrice}
                     onChange={(e) => setMaxPrice(e.target.value)} style={{ width: '90px' }} />
            </label>
            <label>Min. ocjena:
              <select value={minRating} onChange={(e) => setMinRating(Number(e.target.value))}>
                <option value={0}>Sve</option>
                <option value={3}>3+ ★</option>
                <option value={4}>4+ ★</option>
                <option value={4.5}>4.5+ ★</option>
              </select>
            </label>
          </div>

          {/* F1 — amenities checkboxes */}
          <div className="filter-row" style={{ flexWrap: 'wrap' }}>
            <span style={{ color: 'var(--text-tertiary)', fontSize: '0.85em' }}>Sadržaji:</span>
            {[
              { key: 'WIFI', label: '📶 WiFi' },
              { key: 'PARKING', label: '🅿️ Parking' },
              { key: 'AC', label: '❄️ Klima' },
              { key: 'POOL', label: '🏊 Bazen' }
            ].map(a => (
              <label key={a.key} style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.9em' }}>
                <input type="checkbox" checked={amenityFilters[a.key]}
                  onChange={(e) => setAmenityFilters(prev => ({ ...prev, [a.key]: e.target.checked }))} />
                {a.label}
              </label>
            ))}
          </div>

          <form className="filter-row" onSubmit={handleDateSearch}>
            <label>Dolazak:
              <input type="date" value={checkIn} min={today} onChange={(e) => setCheckIn(e.target.value)} />
            </label>
            <label>Odlazak:
              <input type="date" value={checkOut} min={checkIn || today} onChange={(e) => setCheckOut(e.target.value)} />
            </label>
            <button type="submit" className="filter-search-btn" disabled={!checkIn || !checkOut || (!search.trim() && !country)}>
              🔎 Pretraži po periodu
            </button>
            <button type="button" className="filter-reset-btn" onClick={resetFilters}>
              ↺ Reset
            </button>
          </form>
        </div>
      )}

      {loading && <Spinner label="Učitavanje smještaja..." />}
      <ErrorState message={error} onRetry={fetchProperties} />

      {!loading && !error && (
        <>
          <div className="properties-grid">
            {visible.map((property) => (
              <PropertyCard
                key={property.id}
                property={property}
                showWishlist={isGuest}
                wishlisted={!!wishlistItems[property.id]}
                onToggleWishlist={toggleWishlist}
              />
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
