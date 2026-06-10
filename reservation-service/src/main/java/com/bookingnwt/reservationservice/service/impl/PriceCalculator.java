package com.bookingnwt.reservationservice.service.impl;

import com.bookingnwt.reservationservice.client.dto.PricingRuleDTO;
import com.bookingnwt.reservationservice.client.dto.SeasonalRuleDTO;
import com.bookingnwt.reservationservice.model.DiscountType;
import com.bookingnwt.reservationservice.model.PromoCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * F4 + F15 — server-side kalkulacija cijene rezervacije.
 *
 * Mora ostati uskladjena sa frontend kalkulatorom (booking-app/src/utils/pricing.js):
 *   - bazna cijena po noci, vikend cijena (Sub/Ned)
 *   - sezonski modifikatori (procenat na nightly cijenu, sume se po sezoni)
 *   - long-stay popust nakon sezonske korekcije
 *   - promo kod na kraju (procenat ili fiksni iznos)
 *
 * Ako property nema PricingRule, koristi se ista fallback cijena kao u UI
 * (100 BAM/noc) da se klijentski prikaz i server-side obracun poklapaju.
 */
@Component
public class PriceCalculator {

    public static final BigDecimal FALLBACK_PRICE_PER_NIGHT = new BigDecimal("100.00");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /**
     * Ukupna cijena boravka prije promo koda. Baca IllegalArgumentException za
     * krsenje min/max boravka iz cjenovnika ili minimalnog boravka iz sezone.
     */
    public BigDecimal calculateTotal(PricingRuleDTO pricing, List<SeasonalRuleDTO> seasonalRules,
                                     LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new IllegalArgumentException("Datum odlaska mora biti nakon datuma dolaska");
        }

        BigDecimal basePrice = (pricing != null && pricing.getBasePrice() != null)
                ? pricing.getBasePrice() : FALLBACK_PRICE_PER_NIGHT;
        BigDecimal weekendPrice = (pricing != null && pricing.getWeekendPrice() != null)
                ? pricing.getWeekendPrice() : basePrice;

        // F3 — min/max nocenja iz cjenovnika (do sada se nigdje nije provodilo)
        if (pricing != null && pricing.getMinStayDays() != null && nights < pricing.getMinStayDays()) {
            throw new IllegalArgumentException(
                    "Minimalni boravak za ovaj smještaj je " + pricing.getMinStayDays()
                            + " noći (tražili ste " + nights + ")");
        }
        if (pricing != null && pricing.getMaxStayDays() != null && nights > pricing.getMaxStayDays()) {
            throw new IllegalArgumentException(
                    "Maksimalni boravak za ovaj smještaj je " + pricing.getMaxStayDays()
                            + " noći (tražili ste " + nights + ")");
        }

        List<SeasonalRuleDTO> seasons = seasonalRules != null ? seasonalRules : List.of();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (int i = 0; i < nights; i++) {
            LocalDate date = checkIn.plusDays(i);
            DayOfWeek dow = date.getDayOfWeek();
            boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            BigDecimal nightly = isWeekend ? weekendPrice : basePrice;

            BigDecimal seasonalDelta = BigDecimal.ZERO;
            for (SeasonalRuleDTO s : seasons) {
                if (s.getStartDate() == null || s.getEndDate() == null) continue;
                boolean inSeason = !date.isBefore(s.getStartDate()) && !date.isAfter(s.getEndDate());
                if (!inSeason) continue;

                // F15 — minimalni broj nocenja u sezoni se primjenjuje server-side
                if (s.getMinNights() != null && nights < s.getMinNights()) {
                    throw new IllegalArgumentException(
                            "U sezoni \"" + s.getName() + "\" potreban je minimalni boravak "
                                    + s.getMinNights() + " noći (vaš boravak je " + nights + ")");
                }
                int pct = s.getPriceModifierPct() != null ? s.getPriceModifierPct() : 0;
                seasonalDelta = seasonalDelta.add(
                        nightly.multiply(BigDecimal.valueOf(pct)).divide(HUNDRED, 10, RoundingMode.HALF_UP));
            }
            subtotal = subtotal.add(nightly).add(seasonalDelta);
        }

        BigDecimal longStayDiscount = BigDecimal.ZERO;
        if (pricing != null && pricing.getLongStayThreshold() != null && pricing.getLongStayDiscountPct() != null
                && pricing.getLongStayThreshold() > 0 && pricing.getLongStayDiscountPct() > 0
                && nights >= pricing.getLongStayThreshold()) {
            longStayDiscount = subtotal
                    .multiply(BigDecimal.valueOf(pricing.getLongStayDiscountPct()))
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        }

        return subtotal.subtract(longStayDiscount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * F13 — server-side validacija promo koda. Null polja znace "bez ogranicenja"
     * (npr. promo bez maxUses se moze koristiti neograniceno).
     */
    public void validatePromo(PromoCode promo, long nights) {
        LocalDate today = LocalDate.now();
        if (promo.getValidFrom() != null && today.isBefore(promo.getValidFrom())) {
            throw new IllegalArgumentException("Promo kod " + promo.getCode() + " još nije aktivan");
        }
        if (promo.getValidTo() != null && today.isAfter(promo.getValidTo())) {
            throw new IllegalArgumentException("Promo kod " + promo.getCode() + " je istekao");
        }
        if (promo.getMinNights() != null && nights < promo.getMinNights()) {
            throw new IllegalArgumentException(
                    "Promo kod " + promo.getCode() + " zahtijeva minimalno " + promo.getMinNights() + " noćenja");
        }
        if (promo.getMaxUses() != null && promo.getUsageCount() != null
                && promo.getUsageCount() >= promo.getMaxUses()) {
            throw new IllegalArgumentException(
                    "Promo kod " + promo.getCode() + " je iskorišten maksimalan broj puta");
        }
    }

    /** Primjena promo popusta na ukupnu cijenu (procenat ili fiksni iznos). */
    public BigDecimal applyPromo(BigDecimal total, PromoCode promo) {
        BigDecimal discount;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = total.multiply(promo.getDiscountValue()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        } else {
            discount = promo.getDiscountValue().min(total);
        }
        return total.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
