package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event koji Reservation Service emituje kada se kreira nova rezervacija.
 * Property Service sluša ovaj event i markira nekretninu kao nedostupnu.
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
}
