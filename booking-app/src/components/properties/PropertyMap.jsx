import { useMemo } from 'react'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'
import { Link } from 'react-router-dom'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Eksplicit Icon — bez ovoga marker je pomjeren/nevidljiv u Vite bundle-u.
const markerIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

/**
 * F18 — Interaktivna mapa smjestaja. OpenStreetMap kroz Leaflet.
 *
 * Renderuje marker za svaki property koji ima latitude/longitude. Klik na marker
 * → popup sa nazivom, cijenom, dugmetom "Pogledaj detalje". Pomicanje / zoom
 * radi nativno bez backend re-load (svi properties iz parent-a su vec ucitani).
 */
export default function PropertyMap({ properties }) {
  // Filtriraj samo one koji imaju koordinate
  const withCoords = useMemo(
    () => (properties || []).filter(p => p.latitude != null && p.longitude != null),
    [properties]
  )

  // Centar mape: prosjek koordinata, ili Sarajevo kao default
  const center = useMemo(() => {
    if (withCoords.length === 0) return [43.8563, 18.4131] // Sarajevo
    const avgLat = withCoords.reduce((s, p) => s + Number(p.latitude), 0) / withCoords.length
    const avgLng = withCoords.reduce((s, p) => s + Number(p.longitude), 0) / withCoords.length
    return [avgLat, avgLng]
  }, [withCoords])

  if (withCoords.length === 0) {
    return (
      <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-tertiary)' }}>
        Nijedan smještaj nema postavljene koordinate (latitude/longitude).
        <br />
        Mapa će prikazivati objekte čim host doda lokaciju.
      </div>
    )
  }

  return (
    <div style={{ height: '500px', width: '100%', borderRadius: '12px', overflow: 'hidden' }}>
      <MapContainer center={center} zoom={6} style={{ height: '100%', width: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {withCoords.map(p => (
          <Marker key={p.id} position={[Number(p.latitude), Number(p.longitude)]} icon={markerIcon}>
            <Popup>
              <strong>{p.name}</strong>
              <br />
              📍 {p.city}, {p.country}
              <br />
              {p.basePrice != null && <>💰 {Number(p.basePrice).toFixed(2)} BAM/noć<br /></>}
              👥 Do {p.maxGuests} osoba
              <br />
              <Link to={`/properties/${p.id}`} style={{ display: 'inline-block', marginTop: '8px' }}>
                Pogledaj detalje →
              </Link>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  )
}
