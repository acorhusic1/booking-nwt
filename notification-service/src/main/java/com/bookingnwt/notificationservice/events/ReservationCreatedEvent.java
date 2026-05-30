package com.bookingnwt.notificationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BUG 2 — Mirror DTO za ReservationCreatedEvent (od reservation-service).
 * notification-service ga koristi za "Nova rezervacija" notifikaciju hostu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreatedEvent {
    private Long reservationId;
    private Long propertyId;
    private Long userId;          // guestId
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private LocalDateTime eventTimestamp;
    private String eventType;
    private BigDecimal totalPrice;
    private String currency;
    private Long hostId;          // primarni razlog ovog mirror DTO-a
}
