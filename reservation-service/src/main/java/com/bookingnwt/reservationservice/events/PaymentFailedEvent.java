package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event koji reservation-service prima iz payment-service preko RabbitMQ.
 *
 * Ako stigne ovaj event → naplata je propala → INVERZNA AKCIJA: markiraj
 * rezervaciju kao CANCELLED (kompenzacija).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private Long paymentId;
    private Long reservationId;
    private Long propertyId;
    private Long guestId;
    private String reason;
    private LocalDateTime eventTimestamp;
    private String eventType;
}
