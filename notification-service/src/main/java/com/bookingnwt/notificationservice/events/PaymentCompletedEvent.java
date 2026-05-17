package com.bookingnwt.notificationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
