import { Link } from 'react-router-dom'
import '../../styles/PropertyCard.css'

export default function PropertyCard({ property }) {
  return (
    <div className="property-card">
      <div className="property-image">
        <img 
          src={property.image || 'https://via.placeholder.com/300x200'} 
          alt={property.name}
        />
      </div>
      <div className="property-info">
        <h3><Link to={`/properties/${property.id}`}>{property.name}</Link></h3>
        <p className="location">📍 {property.city}</p>
        <p className="description">{property.description?.substring(0, 100)}...</p>
        <div className="property-details">
          <span>🛏️ {property.bedroomCount} spavaća soba</span>
          <span>👥 Do {property.maxGuests} osoba</span>
        </div>
        <div className="property-footer">
          <p className="price">${property.pricePerNight}/noć</p>
          {property.available ? (
            <span className="available">Dostupno</span>
          ) : (
            <span className="unavailable">Nije dostupno</span>
          )}
        </div>
      </div>
    </div>
  )
}

