package com.bookingnwt.reservationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Odgovor property-service GET /api/properties/{id}/seasonal-rules.
 * F15 — sezonska pravila se primjenjuju server-side pri kreiranju rezervacije
 * (korekcija cijene + minimalni broj nocenja u sezoni).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalRuleDTO {
    private Long id;
    private Long propertyId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priceModifierPct;
    private Integer minNights;
}
