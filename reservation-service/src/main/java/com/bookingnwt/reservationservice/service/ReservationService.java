package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.model.ReservationStatus;

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
    void deleteReservation(Long id);
}
