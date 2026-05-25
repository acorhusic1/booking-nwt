package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    ReservationResponseDTO createReservation(ReservationRequestDTO dto);
    ReservationResponseDTO getReservationById(Long id);
    List<ReservationResponseDTO> getAllReservations();
    List<ReservationResponseDTO> getReservationsByGuest(Long guestId);
    List<ReservationResponseDTO> getReservationsByProperty(Long propertyId);
    List<ReservationResponseDTO> getReservationsByHost(Long hostId);
    ReservationResponseDTO updateStatus(Long id, ReservationStatus status);
    ReservationResponseDTO cancelReservation(Long id);
    ReservationResponseDTO updateCancelStatus(Long id, Boolean isCancelled);
    void deleteReservation(Long id);

    // Task 4 — non-trivial endpoints
    ReservationResponseDTO patchReservation(Long id, JsonNode patch);
    Page<ReservationResponseDTO> getReservationsByGuestPaged(Long guestId, Pageable pageable);
    List<ReservationResponseDTO> getReservationsByGuestAndDateRange(Long guestId, LocalDate from, LocalDate to);
    BigDecimal getHostRevenue(Long hostId);
    List<ReservationResponseDTO> batchCreate(List<ReservationRequestDTO> dtos);
    ReservationResponseDTO getReservationWithDetails(Long id);
}
