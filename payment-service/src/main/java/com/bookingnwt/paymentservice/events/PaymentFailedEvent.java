package com.bookingnwt.paymentservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SAGA PATTERN — kompenzacija.
 * Payment-service emituje ovaj event ako naplata padne (npr. nedovoljno
 * sredstava u walletu, valida fail). Reservation-service sluša i poziva
 * INVERZNU AKCIJU — markira rezervaciju kao CANCELLED.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private Long paymentId;
    private Long reservationId;
    private Long guestId;
    private String reason;
    private LocalDateTime eventTimestamp;
    private String eventType = "PAYMENT_FAILED";
}
