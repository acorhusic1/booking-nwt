package com.bookingnwt.reservationservice.service.impl;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ReservationMapper;
import com.bookingnwt.reservationservice.model.CancellationPolicy;
import com.bookingnwt.reservationservice.model.PromoCode;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.bookingnwt.reservationservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final ReservationMapper reservationMapper;

    @Override
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto) {
        Reservation reservation = reservationMapper.toEntity(dto);
        reservation.setStatus(ReservationStatus.CREATED);
        reservation.setCreatedAt(LocalDateTime.now());

        if (dto.getCancellationPolicyId() != null) {
            CancellationPolicy policy = cancellationPolicyRepository.findById(dto.getCancellationPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "CancellationPolicy nije pronađen sa ID: " + dto.getCancellationPolicyId()));
            reservation.setCancellationPolicy(policy);
        }

        if (dto.getPromoCodeId() != null) {
            PromoCode promo = promoCodeRepository.findById(dto.getPromoCodeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PromoCode nije pronađen sa ID: " + dto.getPromoCodeId()));
            reservation.setPromoCode(promo);
        }

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Override
    public ReservationResponseDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id));
        return reservationMapper.toResponseDTO(reservation);
    }

    @Override
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponseDTO> getReservationsByGuest(Long guestId) {
        return reservationRepository.findByGuestId(guestId).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponseDTO> getReservationsByProperty(Long propertyId) {
        return reservationRepository.findByPropertyId(propertyId).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponseDTO> getReservationsByHost(Long hostId) {
        return reservationRepository.findByHostId(hostId).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponseDTO updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id));
        reservation.setStatus(status);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Override
    public ReservationResponseDTO cancelReservation(Long id) {
        return updateStatus(id, ReservationStatus.CANCELLED);
    }

    @Override
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id);
        }
        reservationRepository.deleteById(id);
    }
}
