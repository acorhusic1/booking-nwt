package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event koji reservation-service emituje kada korisnik OTKAŽE rezervaciju
 * (PUT /api/reservations/{id}/cancel).
 *
 * SAGA: dva listenera primaju ovaj event i rade kompenzaciju:
 *   - payment-service: ako je naplata bila COMPLETED, refundira wallet
 *     i markira Payment kao REFUNDED
 *   - property-service: oslobađa kalendar (Property.available = true)
 *
 * Bez ovog event-a, cancel je samo "tihi" status change što je iz
 * korisnikovog ugla bag (pare ostaju skinute, smjestaj ostaje "nedostupno").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelledEvent {
    private Long reservationId;
    private Long propertyId;
    private Long guestId;
    private BigDecimal totalPrice;
    private String currency;
    private String reason;
    private LocalDateTime eventTimestamp;
    private String eventType = "RESERVATION_CANCELLED";
}
