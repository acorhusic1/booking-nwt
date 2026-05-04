package com.bookingnwt.reservationservice.client;

import com.bookingnwt.reservationservice.client.dto.CalendarBlockDTO;
import com.bookingnwt.reservationservice.client.dto.PropertyDTO;
import com.bookingnwt.reservationservice.exception.PropertyUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Resilience4j-protected facade in front of {@link PropertyClient}.
 *
 * <p>When creating a reservation we synchronously ask property-service whether
 * the property exists, is active, and has no overlapping calendar block for the
 * requested window. The call is wrapped in a circuit breaker so a slow or
 * unavailable property-service does not exhaust the reservation thread pool.
 *
 * <p><b>Fallback policy — fail-closed.</b> If property-service is unreachable
 * we deliberately reject the reservation rather than letting an unverified
 * booking through, because allowing it could cause double-booking. The exception
 * is mapped to HTTP 409 Conflict by the global handler so the client sees a
 * clear "try again later" signal and the reservation never persists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyAvailabilityGateway {

    private final PropertyClient propertyClient;

    @CircuitBreaker(name = "property-service", fallbackMethod = "fallback")
    public void verifyAvailable(Long propertyId, LocalDate checkIn, LocalDate checkOut) {
        if (propertyId == null || checkIn == null || checkOut == null) {
            throw new PropertyUnavailableException("Nedostaju propertyId / checkIn / checkOut za provjeru dostupnosti");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new PropertyUnavailableException("checkIn mora biti prije checkOut");
        }

        PropertyDTO property = propertyClient.getProperty(propertyId);
        if (property == null) {
            throw new PropertyUnavailableException("Smještaj " + propertyId + " ne postoji");
        }
        if (Boolean.FALSE.equals(property.getIsActive())) {
            throw new PropertyUnavailableException("Smještaj " + propertyId + " je trenutno neaktivan");
        }

        List<CalendarBlockDTO> blocks = propertyClient.getCalendarBlocks(propertyId);
        if (blocks != null) {
            for (CalendarBlockDTO b : blocks) {
                if (overlaps(b.getStartDate(), b.getEndDate(), checkIn, checkOut)) {
                    throw new PropertyUnavailableException(
                            "Smještaj " + propertyId + " je blokiran u periodu "
                                    + b.getStartDate() + " — " + b.getEndDate()
                                    + (b.getReason() != null ? " (" + b.getReason() + ")" : ""));
                }
            }
        }
    }

    private boolean overlaps(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        if (aStart == null || aEnd == null) return false;
        // half-open interval semantics: [start, end) — the same day a block ends
        // can be re-used as the next reservation's check-in.
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd.plusDays(1));
    }

    @SuppressWarnings("unused")
    private void fallback(Long propertyId, LocalDate checkIn, LocalDate checkOut, Throwable t) {
        if (t instanceof PropertyUnavailableException pue) {
            // Business validation failure — propagate as-is, don't wrap.
            throw pue;
        }
        log.warn("[CircuitBreaker] property-service nedostupan ({}). Odbijam rezervaciju za smještaj {} ({} -> {}).",
                t.getClass().getSimpleName(), propertyId, checkIn, checkOut);
        throw new PropertyUnavailableException(
                "Trenutno ne možemo potvrditi dostupnost smještaja " + propertyId
                        + ". Pokušajte ponovo za nekoliko minuta.");
    }
}
