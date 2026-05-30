package com.bookingnwt.notificationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirror DTO za ReservationCancelledEvent (od reservation-service).
 * notification-service ga koristi za "Otkazana rezervacija" notifikaciju hostu.
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
    private String eventType;
    private Long hostId;
}
