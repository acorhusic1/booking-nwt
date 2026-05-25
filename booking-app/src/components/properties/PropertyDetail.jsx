import { useParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { propertyApi } from "../../api/propertyApi";
import Spinner from "../common/Spinner";
import ErrorState from "../common/ErrorState";
import "../../styles/PropertyDetail.css";

export default function PropertyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [property, setProperty] = useState(null);
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [data, images] = await Promise.all([
        propertyApi.getById(id),
        propertyApi.getImages(id),
      ]);
      setProperty(data);

      if (images && images.length > 0) {
        const primary = images.find((img) => img.isPrimary) || images[0];
        setImageUrl(primary.url);
      }
    } catch {
      setError("Greška pri učitavanju detalja smještaja");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, [id]);

  if (loading) return <Spinner label="Učitavanje smještaja..." size="lg" />;
  if (error) return <ErrorState message={error} onRetry={fetchAll} />;
  if (!property) return <div>Smještaj nije pronađen</div>;

  const getImageUrl = (url, id) => {
    if (!url) return `https://picsum.photos/800/500?random=${id || Math.random()}`;
    if (url.startsWith('http')) return url;
    const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
  };

  const finalImageUrl = getImageUrl(imageUrl || property?.primaryImageUrl, id);

  const handleImageError = (e) => {
    e.target.src = `https://via.placeholder.com/800x500/9d4edd/ffffff?text=${encodeURIComponent(property.name)}`;
  };

  return (
    <div className="property-detail">
      <button onClick={() => navigate(-1)} className="back-btn">
        ← Nazad
      </button>

      <div className="property-detail-container">
        <div className="property-main">
          <h1>{property.name}</h1>
          <p className="location">
            📍 {property.city}, {property.address}
          </p>

          <img
            src={finalImageUrl}
            alt={property.name}
            className="property-image"
            loading="lazy"
            onError={handleImageError}
          />

          <div className="property-description">
            <h3>Opis</h3>
            <p>{property.description}</p>
          </div>

          <div className="property-features">
            <h3>Karakteristike</h3>
            <ul>
              <li>👥 Kapacitet: Do {property.maxGuests} osoba</li>
              <li>🌍 Država: {property.country}</li>
              <li>
                📅 Status:{" "}
                {property.available ? "✅ Dostupno" : "❌ Nije dostupno"}
              </li>
            </ul>
          </div>
        </div>

        <div className="booking-sidebar">
          <button
            className="reserve-btn"
            disabled={!property.available}
            onClick={() => navigate(`/reserve/${id}`)}
          >
            {property.available ? "Rezerviši sada" : "Nije dostupno"}
          </button>

          <div className="reservation-info">
            <p>✅ Besplatna otkazivanja 7 dana prije dolaska</p>
            <p>✅ Brza potvrda rezervacije</p>
          </div>
        </div>
      </div>
    </div>
  );
}
