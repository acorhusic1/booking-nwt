package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event koji reservation-service prima iz payment-service preko RabbitMQ.
 * Mirror DTO — mora se polje-po-polje poklapati sa istim event-om u
 * payment-service-u.
 *
 * Ako stigne ovaj event → naplata je prošla → markiraj rezervaciju kao
 * CONFIRMED (FINAL u Saga-i).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Long paymentId;
    private Long reservationId;
    private Long guestId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime eventTimestamp;
    private String eventType;
}
