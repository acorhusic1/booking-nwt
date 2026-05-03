package com.bookingnwt.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client used by payment-service to synchronously notify reservation-service
 * about payment lifecycle events (e.g. mark a reservation CONFIRMED on successful payment,
 * or back to CREATED on refund). The service name resolves through Eureka — no hardcoded host/port.
 */
@FeignClient(name = "reservation-service", fallback = ReservationClientFallback.class)
public interface ReservationClient {

    @PutMapping("/api/reservations/{id}/status")
    void updateReservationStatus(@PathVariable("id") Long reservationId,
                                 @RequestParam("status") String status);
}
