package com.bookingnwt.paymentservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirror DTO za ReservationCancelledEvent koji emituje reservation-service.
 * Payment-service prima ovaj event preko booking.reservation.cancelled routing
 * key-a i refundira wallet ako je naplata bila COMPLETED.
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
