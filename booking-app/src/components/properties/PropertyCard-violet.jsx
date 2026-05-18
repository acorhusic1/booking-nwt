import { Link } from "react-router-dom";
import "../../styles/PropertyCard.css";

export default function PropertyCard({ property }) {
  // Koristi Picsum Photos sa ID-om svojstva za pouzdane slike
  // Picsum vraća random sliku baziranu na ID-u
  const imageUrl = `https://picsum.photos/400/300?random=${property.id || Math.random()}`;

  // Fallback slike u slučaju da nešto ne radi
  const handleImageError = (e) => {
    e.target.src = `https://via.placeholder.com/400x300/9d4edd/ffffff?text=${encodeURIComponent(property.name)}`;
  };

  return (
    <Link to={`/properties/${property.id}`} className="property-card-link">
      <div className="property-card">
        <div className="property-image">
          <img
            src={imageUrl}
            alt={property.name}
            loading="lazy"
            onError={handleImageError}
          />
        </div>
        <div className="property-info">
          <h3>{property.name}</h3>
          <p className="location">
            📍 {property.city}, {property.country}
          </p>
          <p className="description">
            {property.description?.substring(0, 100)}
            {property.description && property.description.length > 100
              ? "…"
              : ""}
          </p>
          <div className="property-details">
            <span>👥 Do {property.maxGuests} osoba</span>
          </div>
          <div className="property-footer">
            {property.available ? (
              <span className="available">Dostupno</span>
            ) : (
              <span className="unavailable">Nije dostupno</span>
            )}
          </div>
        </div>
      </div>
    </Link>
  );
}
