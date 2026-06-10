package com.bookingnwt.reservationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Odgovor property-service GET /api/properties/{id}/pricing.
 * Koristi se za server-side kalkulaciju cijene rezervacije (F4) —
 * backend vise ne vjeruje totalPrice vrijednosti iz klijentskog request-a.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleDTO {
    private Long id;
    private Long propertyId;
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private Integer minStayDays;
    private Integer maxStayDays;
    private Integer longStayDiscountPct;
    private Integer longStayThreshold;
}
