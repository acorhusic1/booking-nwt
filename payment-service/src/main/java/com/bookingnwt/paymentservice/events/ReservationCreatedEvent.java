package com.bookingnwt.paymentservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event koji payment-service prima iz reservation-service preko RabbitMQ-a.
 * Mirror DTO — mora se polje-po-polje poklapati sa
 * com.bookingnwt.reservationservice.events.ReservationCreatedEvent.
 *
 * SAGA PATTERN: payment-service na osnovu ovog event-a pokreće naplatu.
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
    private String eventType;
    private BigDecimal totalPrice;
    private String currency;
}
