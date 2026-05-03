package com.bookingnwt.paymentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@link ReservationClient} when reservation-service is unreachable
 * or returns errors. We log the failure and swallow it so payment processing can
 * still complete (eventual consistency); a system-events entry / scheduled job
 * would reconcile the reservation status afterwards.
 */
@Slf4j
@Component
public class ReservationClientFallback implements ReservationClient {

    @Override
    public void updateReservationStatus(Long reservationId, String status) {
        log.warn("[Fallback] reservation-service nedostupan; preskačem status update za rezervaciju {} -> {}",
                reservationId, status);
    }
}
