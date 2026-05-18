import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../styles/PropertyDetail.css";
import api from "../../api";

export default function PropertyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [property, setProperty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProperty = async () => {
      try {
        const response = await api.get(`/properties/${id}`);
        setProperty(response.data);
      } catch (err) {
        setError("Greška pri učitavanju smještaja");
      } finally {
        setLoading(false);
      }
    };

    fetchProperty();
  }, [id]);

  if (loading) return <div className="loading">Učitavanje...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!property) return <div>Smještaj nije pronađen</div>;

  // Picsum Photos - pouzdana URL za slike
  const imageUrl = `https://picsum.photos/800/600?random=${property.id || id}`;

  const handleImageError = (e) => {
    e.target.src = `https://via.placeholder.com/800x600/9d4edd/ffffff?text=${encodeURIComponent(property.name)}`;
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
            src={imageUrl}
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

        <aside className="booking-sidebar">
          <div className="price-box">
            <h2>Izvršavanje Rezervacije</h2>
            <p className="from-price">Razmatranje datuma dostupnosti...</p>
          </div>

          {property.available ? (
            <button className="reserve-btn">Rezerviraj Sada</button>
          ) : (
            <p style={{ textAlign: "center", color: "var(--text-tertiary)" }}>
              Ovaj smještaj nije trenutno dostupan
            </p>
          )}
        </aside>
      </div>
    </div>
  );
}
