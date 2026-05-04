package com.bookingnwt.reservationservice.client;

import com.bookingnwt.reservationservice.client.dto.CalendarBlockDTO;
import com.bookingnwt.reservationservice.client.dto.PropertyDTO;
import com.bookingnwt.reservationservice.exception.PropertyUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyAvailabilityGatewayTest {

    @Mock
    private PropertyClient propertyClient;

    @InjectMocks
    private PropertyAvailabilityGateway gateway;

    private PropertyDTO activeProperty() {
        return new PropertyDTO(30L, 99L, "Stan A", "Sarajevo", 4, true);
    }

    @Test
    void verifyAvailable_propertyActiveAndNoOverlap_passes() {
        when(propertyClient.getProperty(30L)).thenReturn(activeProperty());
        when(propertyClient.getCalendarBlocks(30L)).thenReturn(List.of(
                new CalendarBlockDTO(1L, 30L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), "Renoviranje", 99L)
        ));

        assertThatNoException().isThrownBy(() ->
                gateway.verifyAvailable(30L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5)));
    }

    @Test
    void verifyAvailable_propertyMissing_throws() {
        when(propertyClient.getProperty(99L)).thenReturn(null);

        assertThatThrownBy(() -> gateway.verifyAvailable(99L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5)))
                .isInstanceOf(PropertyUnavailableException.class)
                .hasMessageContaining("99");
    }

    @Test
    void verifyAvailable_propertyInactive_throws() {
        PropertyDTO inactive = new PropertyDTO(30L, 99L, "Stan A", "Sarajevo", 4, false);
        when(propertyClient.getProperty(30L)).thenReturn(inactive);

        assertThatThrownBy(() -> gateway.verifyAvailable(30L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5)))
                .isInstanceOf(PropertyUnavailableException.class)
                .hasMessageContaining("neaktivan");
    }

    @Test
    void verifyAvailable_overlappingBlock_throws() {
        when(propertyClient.getProperty(30L)).thenReturn(activeProperty());
        when(propertyClient.getCalendarBlocks(30L)).thenReturn(List.of(
                new CalendarBlockDTO(1L, 30L, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 10), "Renoviranje", 99L)
        ));

        assertThatThrownBy(() -> gateway.verifyAvailable(30L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5)))
                .isInstanceOf(PropertyUnavailableException.class)
                .hasMessageContaining("blokiran");
    }

    @Test
    void verifyAvailable_invalidWindow_throws() {
        assertThatThrownBy(() -> gateway.verifyAvailable(30L, LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 1)))
                .isInstanceOf(PropertyUnavailableException.class)
                .hasMessageContaining("checkIn");
    }

    @Test
    void verifyAvailable_nullArgs_throws() {
        assertThatThrownBy(() -> gateway.verifyAvailable(null, null, null))
                .isInstanceOf(PropertyUnavailableException.class);
    }

    @Test
    void fallback_failClosed_rejectsReservationWhenDownstreamDown() throws Exception {
        // The @CircuitBreaker proxy is not active in a unit test; we invoke the
        // private fallback directly to assert it converts the wrapped failure
        // into a 409 (PropertyUnavailableException). This is the fail-closed
        // policy: if property-service is unreachable, reject the reservation
        // instead of silently allowing an unverified booking.
        Method m = PropertyAvailabilityGateway.class.getDeclaredMethod(
                "fallback", Long.class, LocalDate.class, LocalDate.class, Throwable.class);
        m.setAccessible(true);

        assertThatThrownBy(() -> {
            try {
                m.invoke(gateway, 30L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5),
                        new RuntimeException("connection refused"));
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        })
                .isInstanceOf(PropertyUnavailableException.class)
                .hasMessageContaining("Pokušajte ponovo");
    }

    @Test
    void fallback_propagatesBusinessException() throws Exception {
        // Business validation failures (PropertyUnavailableException) should be
        // re-thrown as-is, not masked behind a generic "service down" message.
        Method m = PropertyAvailabilityGateway.class.getDeclaredMethod(
                "fallback", Long.class, LocalDate.class, LocalDate.class, Throwable.class);
        m.setAccessible(true);

        PropertyUnavailableException original = new PropertyUnavailableException("Smještaj 30 ne postoji");
        assertThatThrownBy(() -> {
            try {
                m.invoke(gateway, 30L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5), original);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        })
                .isInstanceOf(PropertyUnavailableException.class)
                .hasMessageContaining("ne postoji");
    }
}
