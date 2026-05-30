package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.client.PropertyAvailabilityGateway;
import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.PropertyUnavailableException;
import com.bookingnwt.reservationservice.mapper.ReservationMapper;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.bookingnwt.reservationservice.service.impl.ReservationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service-level test for the Task 5 sync-communication contract:
 * createReservation/batchCreate must consult PropertyAvailabilityGateway
 * BEFORE persisting and abort the whole flow if the gateway throws.
 */
@ExtendWith(MockitoExtension.class)
class ReservationAvailabilityIntegrationTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private CancellationPolicyRepository cancellationPolicyRepository;
    @Mock private PromoCodeRepository promoCodeRepository;
    @Mock private ReservationMapper reservationMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private PropertyAvailabilityGateway propertyAvailabilityGateway;

    @InjectMocks private ReservationServiceImpl service;

    private ReservationRequestDTO request;
    private Reservation entity;
    private ReservationResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new ReservationRequestDTO();
        request.setGuestId(10L);
        request.setHostId(20L);
        request.setPropertyId(30L);
        request.setCheckIn(LocalDate.of(2026, 8, 1));
        request.setCheckOut(LocalDate.of(2026, 8, 5));
        request.setNumGuests(2);
        request.setTotalPrice(new BigDecimal("400.00"));

        entity = new Reservation();
        entity.setId(1L);
        entity.setPropertyId(30L);

        response = new ReservationResponseDTO();
        response.setId(1L);
        response.setPropertyId(30L);
    }

    @Test
    void createReservation_callsGatewayBeforeSaving_andSucceeds() {
        when(reservationMapper.toEntity(request)).thenReturn(entity);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(entity);
        when(reservationMapper.toResponseDTO(entity)).thenReturn(response);

        assertThatNoException().isThrownBy(() -> service.createReservation(request));

        verify(propertyAvailabilityGateway, times(1))
                .verifyAvailable(eq(30L), eq(request.getCheckIn()), eq(request.getCheckOut()), any());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void createReservation_propertyUnavailable_aborts_andDoesNotSave() {
        doThrow(new PropertyUnavailableException("Smještaj 30 je blokiran"))
                .when(propertyAvailabilityGateway)
                .verifyAvailable(eq(30L), any(LocalDate.class), any(LocalDate.class), any());

        assertThatThrownBy(() -> service.createReservation(request))
                .isInstanceOf(PropertyUnavailableException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void batchCreate_validatesEveryItem_andOneFailureAbortsAll() {
        ReservationRequestDTO ok = request;

        ReservationRequestDTO blocked = new ReservationRequestDTO();
        blocked.setGuestId(11L);
        blocked.setHostId(20L);
        blocked.setPropertyId(31L);
        blocked.setCheckIn(LocalDate.of(2026, 9, 1));
        blocked.setCheckOut(LocalDate.of(2026, 9, 3));
        blocked.setNumGuests(2);
        blocked.setTotalPrice(new BigDecimal("100.00"));

        // First item is fine, second one fails. We must explicitly stub the OK
        // case as do-nothing so Mockito's strict-stubs doesn't flag the second
        // stub as a mismatch when the first call (propertyId=30) arrives.
        doNothing().when(propertyAvailabilityGateway)
                .verifyAvailable(eq(30L), any(LocalDate.class), any(LocalDate.class), any());
        doThrow(new PropertyUnavailableException("Smještaj 31 je blokiran"))
                .when(propertyAvailabilityGateway)
                .verifyAvailable(eq(31L), any(LocalDate.class), any(LocalDate.class), any());

        assertThatThrownBy(() -> service.batchCreate(List.of(ok, blocked)))
                .isInstanceOf(PropertyUnavailableException.class);

        // First item passed validation, second failed — we never reach the saveAll call.
        verify(propertyAvailabilityGateway).verifyAvailable(eq(30L), any(LocalDate.class), any(LocalDate.class), any());
        verify(propertyAvailabilityGateway).verifyAvailable(eq(31L), any(LocalDate.class), any(LocalDate.class), any());
        verify(reservationRepository, never()).saveAll(anyList());
    }
}
