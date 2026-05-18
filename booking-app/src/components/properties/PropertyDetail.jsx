import { useParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { propertyApi } from "../../api/propertyApi";
import "../../styles/PropertyDetail.css";

export default function PropertyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [property, setProperty] = useState(null);
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAll = async () => {
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
      } catch (err) {
        setError("Greška pri učitavanju detalja smještaja");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchAll();
  }, [id]);

  if (loading) return <div className="loading">Učitavanje...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!property) return <div>Smještaj nije pronađen</div>;

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

          {imageUrl && (
            <img
              src={imageUrl}
              alt={property.name}
              className="property-image"
              loading="lazy"
            />
          )}

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
