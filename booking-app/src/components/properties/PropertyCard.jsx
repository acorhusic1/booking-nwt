import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { propertyApi } from "../../api/propertyApi";
import "../../styles/PropertyCard.css";

export default function PropertyCard({ property }) {
  const [imageUrl, setImageUrl] = useState(null);

  useEffect(() => {
    propertyApi.getImages(property.id).then((images) => {
      if (!images || images.length === 0) return;
      const primary = images.find((img) => img.isPrimary) || images[0];
      setImageUrl(primary.url);
    }).catch(() => {});
  }, [property.id]);

  return (
    <Link to={`/properties/${property.id}`} className="property-card-link">
      <div className="property-card">
        <div className="property-image">
          {imageUrl ? (
            <img src={imageUrl} alt={property.name} loading="lazy" />
          ) : (
            <div className="property-image-placeholder" />
          )}
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
