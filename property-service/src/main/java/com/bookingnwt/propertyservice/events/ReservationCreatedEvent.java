package com.bookingnwt.propertyservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event primljen od Reservation Service-a kada se kreira nova rezervacija.
 * Property Service treba ažurirati dostupnost nekretnine.
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
