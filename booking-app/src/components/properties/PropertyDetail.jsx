import { useParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { propertyApi } from "../../api/propertyApi";
import { reviewApi } from "../../api/reviewApi";
import { messagesApi } from "../../api/messagesApi";
import { userApi } from "../../api/userApi";
import { verificationApi } from "../../api/verificationApi";
import { useAuthStore } from "../../store/authStore";
import { useToast } from "../common/ToastProvider";
import Spinner from "../common/Spinner";
import ErrorState from "../common/ErrorState";
import StarRating from "../common/StarRating";
import "../../styles/PropertyDetail.css";

export default function PropertyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();
  const { showToast } = useToast();
  const [property, setProperty] = useState(null);
  const [imageUrl, setImageUrl] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [contacting, setContacting] = useState(false);
  // F16 — "Verifikovan domaćin" badge (vidljiv gostima)
  const [hostVerified, setHostVerified] = useState(false);

  const startConversation = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if ((user?.role || '').toUpperCase() !== 'GUEST') {
      showToast({ type: 'info', title: 'Samo gosti', message: 'Domaćin ne može sam sebi pisati.' });
      return;
    }
    setContacting(true);
    try {
      const conv = await messagesApi.createConversation(user.id, property.hostId, property.id, null);
      showToast({ type: 'success', title: 'Konverzacija otvorena', message: 'Preusmjeravam vas na poruke...' });
      // BUG F — bez ?conv=X otvorila bi se prva konverzacija u listi
      setTimeout(() => navigate(`/messages?conv=${conv?.id || ''}`), 600);
    } catch (err) {
      showToast({ type: 'error', title: 'Greška', message: err.response?.data?.message || 'Konverzacija nije otvorena.' });
    } finally {
      setContacting(false);
    }
  };

  const fetchAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [data, images, reviewList] = await Promise.all([
        propertyApi.getById(id),
        propertyApi.getImages(id),
        reviewApi.getByProperty(id).catch(() => []),
      ]);
      setProperty(data);

      // F16 — verifikovan status domaćina (tiho preskoci ako endpoint nije dostupan
      // ili korisnik nije ulogovan — badge se jednostavno ne prikaze)
      if (data?.hostId) {
        verificationApi.getVerifiedStatus(data.hostId)
          .then(s => setHostVerified(!!s?.verified))
          .catch(() => setHostVerified(false));
      }

      // BUG 3 — guestName: backend ne populiše, dohvati imena gostiju iz user-service
      const list = Array.isArray(reviewList) ? reviewList : [];
      const uniqueGuestIds = [...new Set(list.map(r => r.guestId).filter(Boolean))];
      const nameMap = {};
      await Promise.all(uniqueGuestIds.map(async (gid) => {
        try {
          const u = await userApi.getById(gid);
          nameMap[gid] = `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.email || `Gost #${gid}`;
        } catch {
          nameMap[gid] = `Gost #${gid}`;
        }
      }));
      setReviews(list.map(r => ({ ...r, guestName: r.guestName || nameMap[r.guestId] || `Gost #${r.guestId}` })));

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

  // F11 — registruj pregled oglasa (fire-and-forget, jednom po otvaranju)
  useEffect(() => {
    if (id) propertyApi.registerView(id).catch(() => {});
  }, [id]);

  // F7 — prosjecna ocjena iz svih recenzija
  const avgRating = reviews.length > 0
    ? reviews.reduce((sum, r) => sum + Number(r.overallRating || 0), 0) / reviews.length
    : 0;

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
    e.target.onerror = null;
    e.target.src = `https://picsum.photos/seed/property-${property?.id || id}/800/500`;
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
          {hostVerified && (
            <p className="host-verified-badge" title="Administrator je potvrdio identitet ovog domaćina">
              🛡️ Verifikovan domaćin
            </p>
          )}

          <img
            src={finalImageUrl}
            alt={property.name}
            className="property-image"
            loading="lazy"
            onError={handleImageError}
          />

          {reviews.length > 0 && (
            <div className="property-rating-summary">
              <StarRating value={avgRating} readOnly size="md" />
              <span className="rating-number">{avgRating.toFixed(1)}</span>
              <span className="rating-count">({reviews.length} {reviews.length === 1 ? 'recenzija' : 'recenzija'})</span>
            </div>
          )}

          <div className="property-description">
            <h3>Opis</h3>
            <p>{property.description}</p>
          </div>

          <div className="property-features">
            <h3>Karakteristike</h3>
            <ul>
              {property.propertyType && (
                <li>
                  🏷 Tip: {{ APARTMAN: 'Apartman', KUCA: 'Kuća za odmor', VILA: 'Vila', HOTEL: 'Hotel', HOSTEL: 'Hostel' }[property.propertyType] || property.propertyType}
                </li>
              )}
              <li>👥 Kapacitet: Do {property.maxGuests} osoba</li>
              <li>🌍 Država: {property.country}</li>
              <li>
                📅 Status:{" "}
                {property.available ? "✅ Dostupno" : "❌ Nije dostupno"}
              </li>
            </ul>
          </div>

          {/* F1/F2 — sadržaji koje je host oznacio pri kreiranju (do sada se
              nigdje nisu prikazivali gostu) */}
          {property.amenities?.length > 0 && (
            <div className="property-features">
              <h3>Sadržaji</h3>
              <div className="amenity-chips">
                {property.amenities.map((a) => (
                  <span key={typeof a === 'string' ? a : a.name} className="amenity-chip">
                    ✓ {typeof a === 'string' ? a : a.name}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* F2 — kućna pravila, bitna gostu PRIJE rezervacije */}
          <div className="property-features">
            <h3>Kućna pravila</h3>
            <div className="amenity-chips">
              <span className={`rule-chip ${property.ruleNoSmoking ? 'rule-no' : 'rule-yes'}`}>
                {property.ruleNoSmoking ? '🚭 Pušenje zabranjeno' : '🚬 Pušenje dozvoljeno'}
              </span>
              <span className={`rule-chip ${property.rulePetsAllowed ? 'rule-yes' : 'rule-no'}`}>
                {property.rulePetsAllowed ? '🐾 Ljubimci dozvoljeni' : '🐾 Ljubimci nisu dozvoljeni'}
              </span>
              <span className={`rule-chip ${property.rulePartiesAllowed ? 'rule-yes' : 'rule-no'}`}>
                {property.rulePartiesAllowed ? '🎉 Žurke dozvoljene' : '🎉 Žurke zabranjene'}
              </span>
              <span className={`rule-chip ${property.ruleChildrenAllowed ? 'rule-yes' : 'rule-no'}`}>
                {property.ruleChildrenAllowed ? '👶 Djeca dobrodošla' : '👶 Nije za djecu'}
              </span>
            </div>
          </div>

          {/* F7 — Recenzije */}
          <div className="property-reviews">
            <h3>Recenzije ({reviews.length})</h3>
            {reviews.length === 0 ? (
              <p className="no-reviews">Još nema recenzija za ovaj smještaj.</p>
            ) : (
              reviews.map((r) => (
                <div key={r.id} className="review-item">
                  <div className="review-item-header">
                    <span className="review-guest">{r.guestName || `Gost #${r.guestId}`}</span>
                    <StarRating value={Number(r.overallRating)} readOnly size="sm" />
                  </div>
                  {r.comment && <p className="review-comment">{r.comment}</p>}
                  <div className="review-categories">
                    <span>Čistoća {Number(r.ratingCleanliness).toFixed(0)}/5</span>
                    <span>Lokacija {Number(r.ratingLocation).toFixed(0)}/5</span>
                    <span>Komunikacija {Number(r.ratingCommunication).toFixed(0)}/5</span>
                    <span>Vrijednost {Number(r.ratingValue).toFixed(0)}/5</span>
                    <span>Tačnost {Number(r.ratingAccuracy).toFixed(0)}/5</span>
                  </div>
                  {r.hostReply && (
                    <div className="review-host-reply">
                      <strong>Odgovor domaćina:</strong> {r.hostReply}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        <div className="booking-sidebar">
          {/* F4 — cijena vidljiva odmah, prije ulaska u rezervaciju */}
          {property.basePrice != null && (
            <p className="sidebar-price">
              <span className="sidebar-price-amount">{Number(property.basePrice).toFixed(0)} BAM</span>
              <span className="sidebar-price-label"> / noć</span>
            </p>
          )}
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

          <button
            className="contact-host-btn"
            onClick={startConversation}
            disabled={contacting}
            style={{ marginTop: '14px', width: '100%' }}
          >
            {contacting ? 'Otvaram...' : '💬 Pošalji poruku domaćinu'}
          </button>
        </div>
      </div>
    </div>
  );
}
