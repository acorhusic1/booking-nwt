package com.bookingnwt.notificationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
