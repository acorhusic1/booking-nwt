package com.bookingnwt.reservationservice.client;

import com.bookingnwt.reservationservice.client.dto.CalendarBlockDTO;
import com.bookingnwt.reservationservice.client.dto.PropertyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client used by reservation-service to synchronously verify that a
 * property exists, is active, and is not blocked for the requested check-in /
 * check-out window before persisting a reservation. The service name resolves
 * via Eureka — no hardcoded host/port.
 */
@FeignClient(name = "property-service")
public interface PropertyClient {

    @GetMapping("/api/properties/{id}")
    PropertyDTO getProperty(@PathVariable("id") Long id);

    @GetMapping("/api/properties/{propertyId}/calendar-blocks")
    List<CalendarBlockDTO> getCalendarBlocks(@PathVariable("propertyId") Long propertyId);
}
