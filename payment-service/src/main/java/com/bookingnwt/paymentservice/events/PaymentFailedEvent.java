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
    // propertyId mora biti uključen jer i property-service prima ovaj event
    // (booking.payment.failed) — koristi ga da oslobodi kalendar (kompenzacija).
    // Bez ovog polja property-service findById(null) baca i poruka ulazi u
    // requeue loop (poison message).
    private Long propertyId;
    private Long guestId;
    private String reason;
    private LocalDateTime eventTimestamp;
    private String eventType = "PAYMENT_FAILED";
    // Host treba notif "Rezervacija otkazana — naplata nije uspjela" da zna
    // da gost nije uspio platiti pa termin ne ostaje "rezervisan" za njega
    private Long hostId;
}
