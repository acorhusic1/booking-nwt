package com.bookingnwt.propertyservice.client;

import com.bookingnwt.propertyservice.client.dto.ReservationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * F7 — sinhrona provjera rezervacije prije kreiranja recenzije.
 * JWT gosta se propagira kroz FeignAuthInterceptor pa reservation-service
 * dozvoljava pristup samo ucesniku rezervacije.
 */
@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @GetMapping("/api/reservations/{id}")
    ReservationDTO getReservation(@PathVariable("id") Long id);
}
