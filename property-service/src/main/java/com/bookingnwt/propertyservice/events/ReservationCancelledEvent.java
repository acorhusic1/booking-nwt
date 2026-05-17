package com.bookingnwt.propertyservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirror DTO za ReservationCancelledEvent koji emituje reservation-service.
 * Property-service prima ovaj event i oslobađa kalendar (available = true).
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
}
