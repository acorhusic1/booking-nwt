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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
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
            promo.setUsageCount(promo.getUsageCount() + 1);
            promoCodeRepository.save(promo);
            reservation.setPromoCode(promo);
        }

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id));
        return reservationMapper.toResponseDTO(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByGuest(Long guestId) {
        return reservationRepository.findByGuestId(guestId).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByProperty(Long propertyId) {
        return reservationRepository.findByPropertyId(propertyId).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByHost(Long hostId) {
        return reservationRepository.findByHostId(hostId).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponseDTO updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id));
        reservation.setStatus(status);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ReservationResponseDTO cancelReservation(Long id) {
        return updateStatus(id, ReservationStatus.CANCELLED);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id);
        }
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReservationResponseDTO patchReservation(Long id, JsonNode patchNode) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id));

        ReservationResponseDTO current = reservationMapper.toResponseDTO(existing);
        try {
            JsonPatch patch = JsonPatch.fromJson(patchNode);
            JsonNode patched = patch.apply(objectMapper.convertValue(current, JsonNode.class));
            ReservationResponseDTO updated = objectMapper.treeToValue(patched, ReservationResponseDTO.class);

            existing.setCheckIn(updated.getCheckIn());
            existing.setCheckOut(updated.getCheckOut());
            existing.setNumGuests(updated.getNumGuests());
            existing.setTotalPrice(updated.getTotalPrice());
            if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
            existing.setUpdatedAt(LocalDateTime.now());

            Reservation saved = reservationRepository.save(existing);
            return reservationMapper.toResponseDTO(saved);
        } catch (JsonPatchException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Neispravna JSON Patch operacija: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Neuspjesno parsiranje patch-a: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> getReservationsByGuestPaged(Long guestId, Pageable pageable) {
        return reservationRepository.findByGuestId(guestId, pageable)
                .map(reservationMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByGuestAndDateRange(Long guestId, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Neispravan opseg datuma: from mora biti prije to");
        }
        return reservationRepository.findByGuestAndDateRange(guestId, from, to).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getHostRevenue(Long hostId) {
        BigDecimal sum = reservationRepository.sumConfirmedRevenueByHost(hostId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public List<ReservationResponseDTO> batchCreate(List<ReservationRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("Lista rezervacija ne smije biti prazna");
        }
        List<Reservation> entities = dtos.stream().map(dto -> {
            Reservation r = reservationMapper.toEntity(dto);
            r.setStatus(ReservationStatus.CREATED);
            r.setCreatedAt(LocalDateTime.now());
            if (dto.getCancellationPolicyId() != null) {
                r.setCancellationPolicy(cancellationPolicyRepository.findById(dto.getCancellationPolicyId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "CancellationPolicy nije pronađen sa ID: " + dto.getCancellationPolicyId())));
            }
            if (dto.getPromoCodeId() != null) {
                r.setPromoCode(promoCodeRepository.findById(dto.getPromoCodeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "PromoCode nije pronađen sa ID: " + dto.getPromoCodeId())));
            }
            return r;
        }).collect(Collectors.toList());

        return reservationRepository.saveAll(entities).stream()
                .map(reservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationWithDetails(Long id) {
        Reservation reservation = reservationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena sa ID: " + id));
        return reservationMapper.toResponseDTO(reservation);
    }
}
