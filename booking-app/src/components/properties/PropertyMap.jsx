import { useMemo, useState, useEffect, useCallback } from 'react'
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet'
import { Link } from 'react-router-dom'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { propertyApi } from '../../api/propertyApi'
import { reviewApi } from '../../api/reviewApi'

/**
 * F18 — Interaktivna mapa smjestaja (OpenStreetMap + Leaflet).
 *
 * Po dokumentaciji:
 *   - markeri prikazuju CIJENU po nocenju direktno na pinu (divIcon "pill")
 *   - pomicanjem/zumiranjem mape se DINAMICKI ucitavaju objekti u vidljivom
 *     podrucju (GET /api/properties/in-bounds na moveend/zoomend)
 *   - klik na marker → popup sa fotografijom, nazivom, ocjenom, cijenom
 *     i linkom na punu stranicu objekta
 */

// Marker "pill" sa cijenom — umjesto genericke Leaflet ikone
const priceIcon = (property) => {
  const label = property.basePrice != null
    ? `${Number(property.basePrice).toFixed(0)} KM`
    : '🏠'
  return L.divIcon({
    className: 'price-marker-wrapper',
    html: `<div class="price-marker">${label}</div>`,
    iconSize: null,
    iconAnchor: [28, 16]
  })
}

const getImageUrl = (property) => {
  const url = property.primaryImageUrl
  if (!url) return `https://picsum.photos/seed/property-${property.id}/300/160`
  if (url.startsWith('http')) return url
  const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

/** Slusa pomicanje/zoom mape i javlja roditelju novi bounding box. */
function ViewportLoader({ onBoundsChange }) {
  const map = useMapEvents({
    moveend: () => onBoundsChange(map.getBounds()),
    zoomend: () => onBoundsChange(map.getBounds())
  })
  return null
}

/** Popup sadrzaj — ocjena se dohvata lijeno, tek kad se popup otvori. */
function MarkerPopupContent({ property }) {
  const [rating, setRating] = useState(null)

  useEffect(() => {
    let cancelled = false
    reviewApi.getByProperty(property.id)
      .then(rs => {
        if (cancelled) return
        const arr = Array.isArray(rs) ? rs : []
        if (arr.length === 0) { setRating({ avg: 0, count: 0 }); return }
        const avg = arr.reduce((s, r) => s + Number(r.overallRating || 0), 0) / arr.length
        setRating({ avg, count: arr.length })
      })
      .catch(() => setRating({ avg: 0, count: 0 }))
    return () => { cancelled = true }
  }, [property.id])

  return (
    <div className="map-popup">
      <img
        src={getImageUrl(property)}
        alt={property.name}
        className="map-popup-img"
        onError={(e) => {
          e.target.onerror = null
          e.target.src = `https://picsum.photos/seed/property-${property.id}/300/160`
        }}
      />
      <strong>{property.name}</strong>
      <div className="map-popup-meta">
        📍 {property.city}, {property.country}
      </div>
      {rating && rating.count > 0 && (
        <div className="map-popup-meta">
          ⭐ {rating.avg.toFixed(1)} ({rating.count} {rating.count === 1 ? 'recenzija' : 'recenzija'})
        </div>
      )}
      <div className="map-popup-meta">
        {property.basePrice != null && <>💰 <strong>{Number(property.basePrice).toFixed(2)} BAM</strong>/noć · </>}
        👥 do {property.maxGuests}
      </div>
      <Link to={`/properties/${property.id}`} className="map-popup-link">
        Pogledaj detalje →
      </Link>
    </div>
  )
}

export default function PropertyMap({ properties }) {
  // Inicijalno markeri iz roditelja; nakon prvog pomicanja mape preuzima
  // dinamicko ucitavanje po viewportu (F18).
  const [mapProperties, setMapProperties] = useState(null) // null = jos koristi props

  const shown = mapProperties ?? (properties || [])
  const withCoords = useMemo(
    () => shown.filter(p => p.latitude != null && p.longitude != null),
    [shown]
  )

  // Centar mape: prosjek koordinata iz inicijalnih propsa, ili Sarajevo
  const center = useMemo(() => {
    const initial = (properties || []).filter(p => p.latitude != null && p.longitude != null)
    if (initial.length === 0) return [43.8563, 18.4131] // Sarajevo
    const avgLat = initial.reduce((s, p) => s + Number(p.latitude), 0) / initial.length
    const avgLng = initial.reduce((s, p) => s + Number(p.longitude), 0) / initial.length
    return [avgLat, avgLng]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleBoundsChange = useCallback((bounds) => {
    const sw = bounds.getSouthWest()
    const ne = bounds.getNorthEast()
    propertyApi.getInBounds(sw.lat, ne.lat, sw.lng, ne.lng)
      .then(list => setMapProperties(Array.isArray(list) ? list : []))
      .catch(() => { /* zadrzi postojece markere ako poziv padne */ })
  }, [])

  return (
    <div style={{ height: '500px', width: '100%', borderRadius: '12px', overflow: 'hidden', position: 'relative' }}>
      <MapContainer center={center} zoom={7} style={{ height: '100%', width: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ViewportLoader onBoundsChange={handleBoundsChange} />
        {withCoords.map(p => (
          <Marker key={p.id} position={[Number(p.latitude), Number(p.longitude)]} icon={priceIcon(p)}>
            <Popup>
              <MarkerPopupContent property={p} />
            </Popup>
          </Marker>
        ))}
      </MapContainer>
      {withCoords.length === 0 && (
        <div className="map-empty-hint">
          Nema smještaja u vidljivom dijelu mape — pomjerite ili odzumirajte mapu.
        </div>
      )}
    </div>
  )
}
