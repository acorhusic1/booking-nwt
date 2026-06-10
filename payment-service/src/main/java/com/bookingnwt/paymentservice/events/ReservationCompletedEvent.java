package com.bookingnwt.paymentservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event koji reservation-service emituje kada boravak ZAVRŠI (status COMPLETED).
 *
 * F19 — payment-service tek tada isplaćuje domaćina: "Domaćin prima isplatu
 * na svoj račun nakon uspješno završenog boravka gosta, umanjenu za proviziju
 * platforme." Ranija isplata odmah pri naplati je ostavljala rupu: kod
 * otkazivanja gost dobije refund, a host bi zadržao payout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCompletedEvent {
    private Long reservationId;
    private Long propertyId;
    private Long guestId;
    private Long hostId;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime eventTimestamp;
    private String eventType = "RESERVATION_COMPLETED";
}
