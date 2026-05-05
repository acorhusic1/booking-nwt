package com.bookingnwt.paymentservice.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationStatusGatewayTest {

    @Mock
    private ReservationClient reservationClient;

    @InjectMocks
    private ReservationStatusGateway gateway;

    @Test
    void updateStatus_delegatesToFeignClient() {
        doNothing().when(reservationClient).updateReservationStatus(10L, "CONFIRMED");

        gateway.updateStatus(10L, "CONFIRMED");

        verify(reservationClient).updateReservationStatus(10L, "CONFIRMED");
    }

    @Test
    void updateStatus_whenFeignThrows_fallbackSwallowsError() throws Exception {
        // Without the @CircuitBreaker proxy active in a unit test the exception
        // would propagate. Resilience4j wires the fallback method by reflection;
        // we invoke it directly to assert it does not rethrow — same behaviour
        // the breaker triggers when OPEN or when the downstream call fails.
        ReservationStatusGateway plain = new ReservationStatusGateway(reservationClient);
        assertThatNoException().isThrownBy(() ->
                invokeFallback(plain, 10L, "CONFIRMED", new RuntimeException("boom")));
    }

    private void invokeFallback(ReservationStatusGateway gateway, Long id, String status, Throwable t)
            throws Exception {
        var m = ReservationStatusGateway.class.getDeclaredMethod(
                "fallback", Long.class, String.class, Throwable.class);
        m.setAccessible(true);
        m.invoke(gateway, id, status, t);
    }
}
