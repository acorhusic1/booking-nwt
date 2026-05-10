package com.bookingnwt.propertyservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Event koji se emituje kada plaćanje padne
 * Property Service trebma osloboditi znanje
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private Long reservationId;
    private Long propertyId;
    private String reason;            // Razlog neuspjeha (insufficient funds, etc.)
    private LocalDateTime eventTimestamp;
}

