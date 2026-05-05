package com.bookingnwt.paymentservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j-protected facade in front of {@link ReservationClient}.
 * Uses Eureka-resolved service name (no hardcoded host/port) and falls back
 * gracefully when the downstream is down or slow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationStatusGateway {

    private final ReservationClient reservationClient;

    @CircuitBreaker(name = "reservation-service", fallbackMethod = "fallback")
    public void updateStatus(Long reservationId, String status) {
        reservationClient.updateReservationStatus(reservationId, status);
    }

    @SuppressWarnings("unused")
    private void fallback(Long reservationId, String status, Throwable t) {
        log.warn("[CircuitBreaker] reservation-service nedostupan ({}). Preskačem status update {} -> {}",
                t.getClass().getSimpleName(), reservationId, status);
    }
}
