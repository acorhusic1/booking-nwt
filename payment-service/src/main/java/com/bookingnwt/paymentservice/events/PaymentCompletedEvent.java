package com.bookingnwt.paymentservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SAGA PATTERN — uspjeh.
 * Payment-service emituje ovaj event nakon što je naplata uspjela.
 * Reservation-service sluša i markira rezervaciju kao CONFIRMED (FINAL).
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
    private String eventType = "PAYMENT_COMPLETED";
}
