import { Link } from "react-router-dom";
import "../../styles/PropertyCard.css";

export default function PropertyCard({ property, showWishlist = false, wishlisted = false, onToggleWishlist }) {
  const getImageUrl = (url, id) => {
    if (!url) return `https://picsum.photos/400/300?random=${id || Math.random()}`;
    if (url.startsWith('http')) return url;
    const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
  };

  const imageUrl = getImageUrl(property.primaryImageUrl, property.id);

  const handleImageError = (e) => {
    // picsum.photos vraca random stock-like fotografiju (bolje od plain tekstualne placehold.co).
    // Koristimo property.id kao seed da svaki smjestaj ima konzistentnu sliku.
    e.target.onerror = null; // sprjecava infinite loop ako i fallback padne
    e.target.src = `https://picsum.photos/seed/property-${property.id}/400/300`;
  };

  return (
    <Link to={`/properties/${property.id}`} className="property-card-link">
      <div className="property-card">
        <div className="property-image">
          {showWishlist && (
            <button
              type="button"
              className="wishlist-heart"
              title={wishlisted ? 'Ukloni iz liste želja' : 'Sačuvaj u listu želja'}
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); onToggleWishlist?.(property.id) }}
            >
              {wishlisted ? '❤️' : '🤍'}
            </button>
          )}
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
            {property.description && property.description.length > 100 ? "…" : ""}
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
