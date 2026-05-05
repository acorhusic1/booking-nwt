package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ReservationMapper;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.client.PropertyAvailabilityGateway;
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

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation reservation;
    private ReservationRequestDTO requestDTO;
    private ReservationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setGuestId(10L);
        reservation.setHostId(20L);
        reservation.setPropertyId(30L);
        reservation.setCheckIn(LocalDate.of(2025, 8, 1));
        reservation.setCheckOut(LocalDate.of(2025, 8, 5));
        reservation.setNumGuests(2);
        reservation.setTotalPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.CREATED);
        reservation.setCreatedAt(LocalDateTime.now());

        requestDTO = new ReservationRequestDTO();
        requestDTO.setGuestId(10L);
        requestDTO.setHostId(20L);
        requestDTO.setPropertyId(30L);
        requestDTO.setCheckIn(LocalDate.of(2025, 8, 1));
        requestDTO.setCheckOut(LocalDate.of(2025, 8, 5));
        requestDTO.setNumGuests(2);
        requestDTO.setTotalPrice(new BigDecimal("500.00"));

        responseDTO = new ReservationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setGuestId(10L);
        responseDTO.setHostId(20L);
        responseDTO.setPropertyId(30L);
        responseDTO.setCheckIn(LocalDate.of(2025, 8, 1));
        responseDTO.setCheckOut(LocalDate.of(2025, 8, 5));
        responseDTO.setNumGuests(2);
        responseDTO.setTotalPrice(new BigDecimal("500.00"));
        responseDTO.setStatus(ReservationStatus.CREATED);
    }

    @Test
    void createReservation_Success() {
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
