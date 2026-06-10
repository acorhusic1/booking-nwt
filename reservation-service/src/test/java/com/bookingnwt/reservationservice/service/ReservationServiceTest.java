package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ReservationMapper;
import com.bookingnwt.reservationservice.model.CancellationPolicy;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.publisher.ReservationEventPublisher;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.client.PropertyAvailabilityGateway;
import com.bookingnwt.reservationservice.events.ReservationCancelledEvent;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.bookingnwt.reservationservice.service.impl.PriceCalculator;
import com.bookingnwt.reservationservice.service.impl.ReservationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private CancellationPolicyRepository cancellationPolicyRepository;
    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PropertyAvailabilityGateway propertyAvailabilityGateway;
    @Mock
    private ReservationEventPublisher reservationEventPublisher;
    // Realan kalkulator (ne mock) — server-side cijena se zaista racuna u testu
    @Spy
    private PriceCalculator priceCalculator = new PriceCalculator();

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation reservation;
    private ReservationRequestDTO requestDTO;
    private ReservationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Datumi u budućnosti — bez ovoga BUG-004 fix bi rušio sve postojeće
        // testove jer cancel sad blokira proteklu check-in.
        LocalDate futureCheckIn = LocalDate.now().plusDays(30);
        LocalDate futureCheckOut = LocalDate.now().plusDays(34);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setGuestId(10L);
        reservation.setHostId(20L);
        reservation.setPropertyId(30L);
        reservation.setCheckIn(futureCheckIn);
        reservation.setCheckOut(futureCheckOut);
        reservation.setNumGuests(2);
        reservation.setTotalPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.CREATED);
        reservation.setCreatedAt(LocalDateTime.now());

        requestDTO = new ReservationRequestDTO();
        requestDTO.setGuestId(10L);
        requestDTO.setHostId(20L);
        requestDTO.setPropertyId(30L);
        requestDTO.setCheckIn(futureCheckIn);
        requestDTO.setCheckOut(futureCheckOut);
        requestDTO.setNumGuests(2);
        requestDTO.setTotalPrice(new BigDecimal("500.00"));

        responseDTO = new ReservationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setGuestId(10L);
        responseDTO.setHostId(20L);
        responseDTO.setPropertyId(30L);
        responseDTO.setCheckIn(futureCheckIn);
        responseDTO.setCheckOut(futureCheckOut);
        responseDTO.setNumGuests(2);
        responseDTO.setTotalPrice(new BigDecimal("500.00"));
        responseDTO.setStatus(ReservationStatus.CREATED);
    }

    @Test
    void createReservation_Success() {
        when(reservationRepository.existsOverlap(any(), any(), any())).thenReturn(false);
        when(reservationMapper.toEntity(requestDTO)).thenReturn(reservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.createReservation(requestDTO);

        assertNotNull(result);
        assertEquals(10L, result.getGuestId());
        assertEquals(ReservationStatus.CREATED, result.getStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_BlockedWhenOverlapExists() {
        // BUG-001: double-submit kroz Back dugme — local overlap check mora hvatati
        when(reservationRepository.existsOverlap(any(), any(), any())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(requestDTO));
        assertTrue(ex.getMessage().toLowerCase().contains("rezervacija"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void getReservationById_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.getReservationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getReservationById_NotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reservationService.getReservationById(99L));
    }

    @Test
    void getAllReservations_Success() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        List<ReservationResponseDTO> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
    }

    @Test
    void getReservationsByGuest_Success() {
        when(reservationRepository.findByGuestId(10L)).thenReturn(List.of(reservation));
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        List<ReservationResponseDTO> result = reservationService.getReservationsByGuest(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getGuestId());
    }

    @Test
    void getReservationsByProperty_Success() {
        when(reservationRepository.findByPropertyId(30L)).thenReturn(List.of(reservation));
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        List<ReservationResponseDTO> result = reservationService.getReservationsByProperty(30L);

        assertEquals(1, result.size());
    }

    @Test
    void getReservationsByHost_Success() {
        when(reservationRepository.findByHostId(20L)).thenReturn(List.of(reservation));
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        List<ReservationResponseDTO> result = reservationService.getReservationsByHost(20L);

        assertEquals(1, result.size());
    }

    @Test
    void updateStatus_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        responseDTO.setStatus(ReservationStatus.CONFIRMED);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.updateStatus(1L, ReservationStatus.CONFIRMED);

        assertNotNull(result);
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void cancelReservation_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        responseDTO.setStatus(ReservationStatus.CANCELLED);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelReservation_BlockedWhenCompleted() {
        reservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservationService.cancelReservation(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("završena"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_BlockedWhenActive() {
        reservation.setStatus(ReservationStatus.ACTIVE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservationService.cancelReservation(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("support"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_BlockedWhenCheckInAlreadyPassed() {
        reservation.setCheckIn(LocalDate.now().minusDays(1));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservationService.cancelReservation(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("prošao"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_NoRefundWhenConfirmedAndLessThanFreeCancelDaysAway() {
        // F6 — gost MOŽE otkazati u bilo kojem trenutku (dokumentacija), ali
        // unutar free-cancel prozora (3 dana < default 7) refund je 0%.
        reservation.setCheckIn(LocalDate.now().plusDays(3));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        responseDTO.setStatus(ReservationStatus.CANCELLED);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        ArgumentCaptor<ReservationCancelledEvent> captor =
                ArgumentCaptor.forClass(ReservationCancelledEvent.class);
        verify(reservationEventPublisher).publishReservationCancelled(captor.capture());
        assertEquals(0, captor.getValue().getRefundPercentage());
    }

    @Test
    void cancelReservation_AllowsCreatedEvenInsideFreeCancelWindow() {
        // CREATED status (Saga još nije završila) sa samo 2 dana do check-in
        // — abort je siguran bez obzira na cancellation window jer payment
        // možda još nije ni prošao.
        reservation.setCheckIn(LocalDate.now().plusDays(2));
        reservation.setStatus(ReservationStatus.CREATED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        responseDTO.setStatus(ReservationStatus.CANCELLED);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.cancelReservation(1L);
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelReservation_HonorsPerPropertyCancellationPolicy() {
        // Property ima policy sa freeCancelDays=14, partialRefundPct=40 —
        // cancel 10 dana prije check-ina je UNUTAR strict prozora pa se
        // primjenjuje djelimican refund umjesto punog (default 7 bi dao 100%).
        CancellationPolicy policy = new CancellationPolicy(30L, "Strict", 14, 40, false);
        reservation.setCancellationPolicy(policy);
        reservation.setCheckIn(LocalDate.now().plusDays(10));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        responseDTO.setStatus(ReservationStatus.CANCELLED);
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        ReservationResponseDTO result = reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        ArgumentCaptor<ReservationCancelledEvent> captor =
                ArgumentCaptor.forClass(ReservationCancelledEvent.class);
        verify(reservationEventPublisher).publishReservationCancelled(captor.capture());
        assertEquals(40, captor.getValue().getRefundPercentage());
    }

    @Test
    void deleteReservation_Success() {
        when(reservationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(1L);

        assertDoesNotThrow(() -> reservationService.deleteReservation(1L));
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void deleteReservation_NotFound() {
        when(reservationRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> reservationService.deleteReservation(99L));
    }
}
