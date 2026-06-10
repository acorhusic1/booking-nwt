import { useForm, useWatch } from 'react-hook-form'
import { useState, useEffect } from 'react'
import { reservationApi } from '../../api/reservationApi'
import { propertyApi } from '../../api/propertyApi'
import { promoApi } from '../../api/promoApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../common/ToastProvider'
import { calculateReservationPrice } from '../../utils/pricing'
import { todayLocalISO } from '../../utils/dates'
import GuestDatePicker from './GuestDatePicker'
import '../../styles/ReservationForm.css'

// Fallback ako property nema PricingRule postavljen (host nije konfigurisao)
const FALLBACK_PRICE_PER_NIGHT_BAM = 100

export default function ReservationForm({ propertyId }) {
  const { register, handleSubmit, control, setValue, formState: { errors } } = useForm()
  const { user, isAuthenticated } = useAuthStore()
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [property, setProperty] = useState(null)
  // F4 — Pricing rule
  const [pricing, setPricing] = useState(null)
  // F15 — Sezonska pravila
  const [seasonalRules, setSeasonalRules] = useState([])

  // F13 — Promo kod
  const [promoInput, setPromoInput] = useState('')
  const [appliedPromo, setAppliedPromo] = useState(null)
  const [promoError, setPromoError] = useState(null)
  const [promoLoading, setPromoLoading] = useState(false)

  const checkIn = useWatch({ control, name: 'checkIn' })
  const checkOut = useWatch({ control, name: 'checkOut' })

  // BUG-009: minimum date u input-u — sprjecava odabir proslog datuma
  // (lokalna zona — toISOString() bi izmedju ponoci i 02:00 vratio jucerasnji dan)
  const today = todayLocalISO()
  const minCheckOut = checkIn || today

  // hostId trebamo backendu — uzimamo ga iz property-a
  useEffect(() => {
    if (!propertyId) return
    propertyApi.getById(propertyId)
      .then(setProperty)
      .catch(() => setError('Greška pri učitavanju smještaja'))
    // F4 — fetch pricing rule (404 → fallback flat rate)
    propertyApi.getPricing(propertyId)
      .then(setPricing)
      .catch(() => setPricing(null))
    // F15 — sezonska pravila (prazna lista ako nema)
    propertyApi.getSeasonalRules(propertyId)
      .then(rules => setSeasonalRules(rules || []))
      .catch(() => setSeasonalRules([]))
  }, [propertyId])

  // F4 — koristi smart calculator (basePrice + weekendPrice + longStayDiscount)
  // Ako host nije postavio PricingRule, fallback na flat 100 BAM/noć.
  const effectivePricing = pricing || {
    basePrice: FALLBACK_PRICE_PER_NIGHT_BAM,
    weekendPrice: FALLBACK_PRICE_PER_NIGHT_BAM,
    longStayThreshold: 0,
    longStayDiscountPct: 0
  }
  const calc = calculateReservationPrice(effectivePricing, checkIn, checkOut, seasonalRules)
  const nights = calc.nights
  const subtotal = calc.subtotal
  const longStayDiscount = calc.longStayDiscount
  const seasonalAdjustment = calc.seasonalAdjustment
  const activeSeasons = calc.activeSeasons
  const minNightsViolation = calc.minNightsViolation
  const stayViolation = calc.stayViolation

  // F13 — obracun popusta iz primijenjenog promo koda (na subtotal nakon long-stay)
  const afterLongStay = subtotal - longStayDiscount
  const promoDiscount = (() => {
    if (!appliedPromo || afterLongStay <= 0) return 0
    if (appliedPromo.discountType === 'PERCENTAGE') {
      return Math.round(afterLongStay * (Number(appliedPromo.discountValue) / 100) * 100) / 100
    }
    return Math.min(Number(appliedPromo.discountValue), afterLongStay)
  })()

  const totalPrice = Math.max(0, afterLongStay - promoDiscount)

  const applyPromo = async () => {
    const code = promoInput.trim().toUpperCase()
    if (!code) return
    setPromoError(null)
    setPromoLoading(true)
    try {
      const promo = await promoApi.validateByCode(code)

      // Klijentska validacija uslova (backend takodjer validira pri kreiranju)
      const todayDate = new Date(today)
      if (promo.validFrom && new Date(promo.validFrom) > todayDate) {
        throw new Error('Promo kod još nije aktivan')
      }
      if (promo.validTo && new Date(promo.validTo) < todayDate) {
        throw new Error('Promo kod je istekao')
      }
      if (promo.minNights && nights > 0 && nights < promo.minNights) {
        throw new Error(`Promo kod zahtijeva minimalno ${promo.minNights} noćenja`)
      }
      if (promo.maxUses != null && promo.usageCount != null && promo.usageCount >= promo.maxUses) {
        throw new Error('Promo kod je iskorišten maksimalan broj puta')
      }

      setAppliedPromo(promo)
      showToast({ type: 'success', title: 'Promo kod primijenjen',
        message: `${promo.code} — ${promo.discountType === 'PERCENTAGE' ? promo.discountValue + '%' : promo.discountValue + ' BAM'} popusta.` })
    } catch (err) {
      setAppliedPromo(null)
      const msg = err.response?.status === 404
        ? 'Promo kod ne postoji'
        : (err.response?.data?.message || err.message || 'Promo kod nije validan')
      setPromoError(msg)
    } finally {
      setPromoLoading(false)
    }
  }

  const removePromo = () => {
    setAppliedPromo(null)
    setPromoInput('')
    setPromoError(null)
  }

  const onSubmit = async (data) => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    if (!property) {
      setError('Smještaj još nije učitan')
      return
    }
    if (nights <= 0) {
      setError('Datum odlaska mora biti nakon datuma dolaska')
      return
    }
    // maxGuests guard (frontend) — backend dodatno ovo validira preko PropertyAvailabilityGateway
    if (property.maxGuests && Number(data.numGuests) > property.maxGuests) {
      const msg = `Smještaj prima maksimalno ${property.maxGuests} osoba. Vi ste unijeli ${data.numGuests}.`
      setError(msg)
      showToast({ type: 'error', title: 'Prekoračen kapacitet', message: msg, duration: 7000 })
      return
    }
    // F15 — minNights iz sezone
    if (minNightsViolation) {
      const msg = `U sezoni "${minNightsViolation.season}" potreban je minimalni boravak ${minNightsViolation.required} noći (vi imate ${minNightsViolation.actual}).`
      setError(msg)
      showToast({ type: 'warning', title: 'Sezonska ograničenja', message: msg, duration: 7000 })
      return
    }
    // F3 — min/max boravak iz cjenovnika (backend bi vratio 400, presretni ranije)
    if (stayViolation) {
      const msg = stayViolation.type === 'min'
        ? `Minimalni boravak za ovaj smještaj je ${stayViolation.required} noći (izabrali ste ${stayViolation.actual}).`
        : `Maksimalni boravak za ovaj smještaj je ${stayViolation.required} noći (izabrali ste ${stayViolation.actual}).`
      setError(msg)
      showToast({ type: 'warning', title: 'Ograničenje boravka', message: msg, duration: 7000 })
      return
    }

    setLoading(true)
    setError(null)
    try {
      const reservationData = {
        guestId: user.id,
        hostId: property.hostId,
        propertyId: Number(propertyId),
        checkIn: data.checkIn,
        checkOut: data.checkOut,
        numGuests: Number(data.numGuests),
        totalPrice: totalPrice,
        promoCodeId: appliedPromo?.id ?? null
      }
      const created = await reservationApi.create(reservationData)
      // BUG-001: replace:true sprjecava da Back dugme vrati korisnika
      // nazad na rezervacionu formu (pa da je submit-uje ponovo)
      navigate(`/dashboard?tab=reservations&pendingId=${created.id}&pending=1`, { replace: true })
    } catch (err) {
      const msg = err.response?.data?.message
      const text = typeof msg === 'string' ? msg : 'Greška pri kreiranju rezervacije'
      setError(text)
      showToast({ type: 'error', title: 'Rezervacija nije kreirana', message: text, duration: 7000 })
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="reservation-form">
      <h3>Rezerviši smještaj</h3>

      {/* Cijena vidljiva ODMAH, prije izbora datuma */}
      <div className="price-preview">
        <span className="price-preview-amount">
          {Number(effectivePricing.basePrice).toFixed(0)} BAM
        </span>
        <span className="price-preview-label"> / noć</span>
        {pricing && Number(pricing.weekendPrice) > 0
          && Number(pricing.weekendPrice) !== Number(pricing.basePrice) && (
          <span className="price-preview-weekend"> (vikend {Number(pricing.weekendPrice).toFixed(0)} BAM)</span>
        )}
        {pricing?.minStayDays > 1 && (
          <span className="price-preview-minstay"> · min. {pricing.minStayDays} noći</span>
        )}
        {!pricing && <span className="price-preview-weekend"> (standardna cijena)</span>}
      </div>

      {/* BUG 8 — Vizualni kalendar sa zauzetim datumima */}
      {property && (
        <div className="form-group">
          <label>📅 Izaberi datume na kalendaru:</label>
          <GuestDatePicker
            propertyId={propertyId}
            hostId={property.hostId}
            value={{ checkIn, checkOut }}
            onChange={({ checkIn: ci, checkOut: co }) => {
              setValue('checkIn', ci, { shouldValidate: true })
              setValue('checkOut', co, { shouldValidate: true })
            }}
          />
        </div>
      )}

      <div className="form-group">
        <label>Datum dolaska:</label>
        <input
          {...register('checkIn', { required: 'Obavezno polje' })}
          type="date"
          min={today}
        />
        {errors.checkIn && <span className="error">{errors.checkIn.message}</span>}
      </div>

      <div className="form-group">
        <label>Datum odlaska:</label>
        <input
          {...register('checkOut', { required: 'Obavezno polje' })}
          type="date"
          min={minCheckOut}
        />
        {errors.checkOut && <span className="error">{errors.checkOut.message}</span>}
      </div>

      <div className="form-group">
        <label>Broj osoba:{property?.maxGuests && <em style={{color: 'var(--text-tertiary)', fontWeight: 'normal'}}> (max {property.maxGuests})</em>}</label>
        <input
          {...register('numGuests', {
            required: 'Obavezno polje',
            min: { value: 1, message: 'Minimalno 1 gost' },
            max: property?.maxGuests
              ? { value: property.maxGuests, message: `Maksimalno ${property.maxGuests} osoba (kapacitet smještaja)` }
              : undefined,
            valueAsNumber: true
          })}
          type="number"
          min="1"
          max={property?.maxGuests || 20}
          defaultValue={1}
        />
        {errors.numGuests && <span className="error">{errors.numGuests.message}</span>}
      </div>

      {/* F13 — Promo kod */}
      <div className="form-group promo-group">
        <label>Promo kod (opcionalno):</label>
        {!appliedPromo ? (
          <div className="promo-input-row">
            <input
              type="text"
              placeholder="npr. LJETO2026"
              value={promoInput}
              onChange={(e) => setPromoInput(e.target.value)}
              disabled={promoLoading}
            />
            <button
              type="button"
              className="promo-apply-btn"
              onClick={applyPromo}
              disabled={promoLoading || !promoInput.trim()}
            >
              {promoLoading ? '...' : 'Primijeni'}
            </button>
          </div>
        ) : (
          <div className="promo-applied">
            <span>✅ <strong>{appliedPromo.code}</strong> — {appliedPromo.discountType === 'PERCENTAGE' ? `${appliedPromo.discountValue}%` : `${appliedPromo.discountValue} BAM`} popust</span>
            <button type="button" className="promo-remove-btn" onClick={removePromo}>Ukloni</button>
          </div>
        )}
        {promoError && <span className="error">{promoError}</span>}
      </div>

      {activeSeasons.length > 0 && (
        <div className="seasonal-banner">
          🗓 Aktivne sezone u ovom periodu: <strong>{activeSeasons.join(', ')}</strong>
          {seasonalAdjustment !== 0 && (
            <span> — cijena {seasonalAdjustment > 0 ? 'uvećana' : 'umanjena'} za {Math.abs(seasonalAdjustment).toFixed(2)} BAM</span>
          )}
        </div>
      )}

      {minNightsViolation && (
        <div className="error-alert">
          ⚠ Sezona "{minNightsViolation.season}" traži minimum {minNightsViolation.required} noći (vaš boravak je {minNightsViolation.actual}).
        </div>
      )}

      {stayViolation && (
        <div className="error-alert">
          ⚠ {stayViolation.type === 'min'
            ? `Minimalni boravak za ovaj smještaj je ${stayViolation.required} noći (izabrali ste ${stayViolation.actual}).`
            : `Maksimalni boravak za ovaj smještaj je ${stayViolation.required} noći (izabrali ste ${stayViolation.actual}).`}
        </div>
      )}

      <div className="summary summary-breakdown">
        {nights > 0 ? (
          <>
            <div className="summary-line">
              <span>
                {nights} noći
                {pricing && Number(pricing.basePrice) !== Number(pricing.weekendPrice) && (
                  <em className="summary-sub"> (radni {pricing.basePrice} BAM, vikend {pricing.weekendPrice} BAM)</em>
                )}
                {!pricing && <em className="summary-sub"> × {FALLBACK_PRICE_PER_NIGHT_BAM} BAM/noć</em>}
                {seasonalAdjustment !== 0 && (
                  <em className="summary-sub"> sezonska korekcija: {seasonalAdjustment > 0 ? '+' : ''}{seasonalAdjustment.toFixed(2)} BAM</em>
                )}
              </span>
              <span>{subtotal.toFixed(2)} BAM</span>
            </div>
            {longStayDiscount > 0 && (
              <div className="summary-line discount-line">
                <span>Long-stay popust ({pricing.longStayDiscountPct}% za {pricing.longStayThreshold}+ noći)</span>
                <span>−{longStayDiscount.toFixed(2)} BAM</span>
              </div>
            )}
            {promoDiscount > 0 && (
              <div className="summary-line discount-line">
                <span>Promo popust ({appliedPromo.code})</span>
                <span>−{promoDiscount.toFixed(2)} BAM</span>
              </div>
            )}
            <div className="summary-line summary-total">
              <span>Ukupno</span>
              <span><strong>{totalPrice.toFixed(2)} BAM</strong></span>
            </div>
          </>
        ) : (
          <div className="summary-line">
            <span>Odaberi datume — cijena je {Number(effectivePricing.basePrice).toFixed(0)} BAM/noć</span>
            <span>—</span>
          </div>
        )}
      </div>

      {error && <div className="error-alert">{error}</div>}

      <button type="submit" disabled={loading || !property || nights <= 0 || !!stayViolation || !!minNightsViolation} className="submit-btn">
        {loading ? 'Rezerviranje...' : 'Potvrdi rezervaciju'}
      </button>
    </form>
  )
}
