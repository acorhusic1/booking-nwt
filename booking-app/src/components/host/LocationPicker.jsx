import { useState } from 'react'
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Custom icon — eksplicitno iconSize + iconAnchor da marker pin sjedne TAČNO
// na klik lokaciju. Bez ovoga Vite/Rollup ne resolvuje default Leaflet slike
// pa marker bude pomaknut/nevidljiv.
const markerIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],     // donji vrh pin-a je na lokaciji
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

/**
 * Klik na mapu → postavlja marker + poziva onPick({lat, lng}).
 * Default centar prikazuje cijelu BiH+region.
 */
function ClickHandler({ onClick }) {
  useMapEvents({
    click(e) { onClick(e.latlng.lat, e.latlng.lng) }
  })
  return null
}

function hasValidCoord(v) {
  if (!v) return false
  const lat = Number(v.lat)
  const lng = Number(v.lng)
  // Empty string i null daju NaN — to mora pasti
  return Number.isFinite(lat) && Number.isFinite(lng) && (lat !== 0 || lng !== 0)
}

export default function LocationPicker({ value, onChange }) {
  const [pos, setPos] = useState(
    hasValidCoord(value) ? [Number(value.lat), Number(value.lng)] : null
  )
  // Default: centar BiH, dovoljno odzumirano da vidimo cijelu zemlju + susjede
  const center = pos || [43.9, 17.7]
  const initialZoom = pos ? 14 : 7

  const handlePick = (lat, lng) => {
    setPos([lat, lng])
    onChange?.({ lat: lat.toFixed(6), lng: lng.toFixed(6) })
  }

  return (
    <div>
      <div style={{ height: '320px', borderRadius: '8px', overflow: 'hidden', marginBottom: '8px' }}>
        <MapContainer center={center} zoom={initialZoom} style={{ height: '100%', width: '100%' }}>
          <TileLayer
            attribution='&copy; OpenStreetMap'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ClickHandler onClick={handlePick} />
          {pos && <Marker position={pos} icon={markerIcon} />}
        </MapContainer>
      </div>
      <p style={{ fontSize: '0.85em', color: 'var(--text-tertiary)', margin: 0 }}>
        {pos
          ? <>📍 Lokacija: <strong>{pos[0].toFixed(4)}, {pos[1].toFixed(4)}</strong> — klikni drugo mjesto da promijeniš.</>
          : <>🖱 Klikni na mapu da označiš tačnu lokaciju smještaja.</>
        }
      </p>
    </div>
  )
}
