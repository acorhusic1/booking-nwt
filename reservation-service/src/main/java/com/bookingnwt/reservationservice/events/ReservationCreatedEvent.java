package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event koji reservation-service emituje kada se kreira nova rezervacija.
 *
 * SLUŠAJU GA DVA SERVISA (Saga choreography):
 *   - property-service: blokira termine na kalendaru
 *   - payment-service:  pokreće naplatu (Task 3 - reservation+payment Saga)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreatedEvent {
    private Long reservationId;
    private Long propertyId;
    private Long userId;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private LocalDateTime eventTimestamp;
    private String eventType = "RESERVATION_CREATED";
    // Task 3 — payment-service koristi ova polja za naplatu
    private BigDecimal totalPrice;
    private String currency;
    // BUG 2 — notification-service treba hostId za "Nova rezervacija" notifikaciju
    private Long hostId;
}
