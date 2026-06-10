package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event koji reservation-service emituje kada boravak ZAVRŠI (scheduler
 * prebaci rezervaciju u COMPLETED).
 *
 * F19 — payment-service sluša ovaj event i tek tada isplaćuje domaćina
 * (umanjeno za proviziju platforme). Isplata pri samoj naplati je bila
 * pogrešna: kod otkazivanja gost dobije refund, a host bi zadržao payout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCompletedEvent {
    private Long reservationId;
    private Long propertyId;
    private Long guestId;
    private Long hostId;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime eventTimestamp;
    private String eventType = "RESERVATION_COMPLETED";
}
