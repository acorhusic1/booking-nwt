package com.bookingnwt.reservationservice.service.impl;

import com.bookingnwt.reservationservice.client.PropertyAvailabilityGateway;
import com.bookingnwt.reservationservice.client.dto.PricingRuleDTO;
import com.bookingnwt.reservationservice.client.dto.SeasonalRuleDTO;
import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.events.ReservationCancelledEvent;
import com.bookingnwt.reservationservice.events.ReservationCreatedEvent;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ReservationMapper;
import com.bookingnwt.reservationservice.model.CancellationPolicy;
import com.bookingnwt.reservationservice.model.PromoCode;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.publisher.ReservationEventPublisher;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final PropertyAvailabilityGateway propertyAvailabilityGateway;
    private final ReservationEventPublisher reservationEventPublisher;
    private final PriceCalculator priceCalculator;

    @Override
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto) {
        // BUG-010: Realistic cap — ne dozvoli rezervaciju duzu od 30 dana
        if (dto.getCheckIn() != null && dto.getCheckOut() != null) {
            long days = ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());
            if (days <= 0) {
                throw new IllegalArgumentException("Datum odlaska mora biti nakon datuma dolaska");
            }
            if (days > 30) {
                throw new IllegalArgumentException(
                        "Maksimalna duzina rezervacije je 30 dana (tražili ste " + days + ")");
            }
        }

        // BUG-001: Lokalni overlap check prije Saga-e. Hvata double-submit
        // (npr. Back dugme → resubmit) kada property-service jos nije primio
        // Rabbit event pa availability-gateway misli da je slobodno.
        boolean overlap = reservationRepository.existsOverlap(
                dto.getPropertyId(), dto.getCheckIn(), dto.getCheckOut());
        if (overlap) {
            throw new IllegalStateException(
                    "Već postoji aktivna rezervacija za ovaj smještaj u datim datumima");
        }

        // Task 5 — synchronous availability check against property-service.
        // Throws PropertyUnavailableException -> 409 Conflict if the property
        // doesn't exist, is inactive, is blocked on the calendar, or
        // property-service is unreachable (fail-closed via circuit breaker).
        propertyAvailabilityGateway.verifyAvailable(
                dto.getPropertyId(), dto.getCheckIn(), dto.getCheckOut(), dto.getNumGuests());

        // K1 (F4/F15) — server-side kalkulacija cijene. Backend je autoritativan:
        // klijentski totalPrice se ne koristi za naplatu, samo se loguje odstupanje.
        long nights = ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());
        PricingRuleDTO pricing = propertyAvailabilityGateway.fetchPricing(dto.getPropertyId());
        List<SeasonalRuleDTO> seasonalRules = propertyAvailabilityGateway.fetchSeasonalRules(dto.getPropertyId());
        BigDecimal computedTotal = priceCalculator.calculateTotal(
                pricing, seasonalRules, dto.getCheckIn(), dto.getCheckOut());

        Reservation reservation = reservationMapper.toEntity(dto);
        reservation.setStatus(ReservationStatus.CREATED);
        reservation.setCreatedAt(LocalDateTime.now());

        // K12 (F6) — politika otkazivanja: eksplicitna mora pripadati ovom property-ju,
        // inace se automatski veze politika koju je host definisao za property.
        if (dto.getCancellationPolicyId() != null) {
            CancellationPolicy policy = cancellationPolicyRepository.findById(dto.getCancellationPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "CancellationPolicy nije pronađen sa ID: " + dto.getCancellationPolicyId()));
            if (policy.getPropertyId() != null && !policy.getPropertyId().equals(dto.getPropertyId())) {
                throw new IllegalArgumentException("Politika otkazivanja ne pripada ovom smještaju");
            }
            reservation.setCancellationPolicy(policy);
        } else {
            cancellationPolicyRepository.findByPropertyId(dto.getPropertyId()).stream()
                    .findFirst()
                    .ifPresent(reservation::setCancellationPolicy);
        }

        // K2 (F13) — promo kod se validira server-side (period vazenja, max broj
        // koristenja, minimalan broj nocenja) i popust se obracunava ovdje.
        if (dto.getPromoCodeId() != null) {
            PromoCode promo = promoCodeRepository.findById(dto.getPromoCodeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PromoCode nije pronađen sa ID: " + dto.getPromoCodeId()));
            priceCalculator.validatePromo(promo, nights);
            computedTotal = priceCalculator.applyPromo(computedTotal, promo);
            promo.setUsageCount(promo.getUsageCount() + 1);
            promoCodeRepository.save(promo);
            reservation.setPromoCode(promo);
        }

        if (dto.getTotalPrice() != null
                && dto.getTotalPrice().subtract(computedTotal).abs().compareTo(new BigDecimal("0.05")) > 0) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "⚠️ Klijentska cijena {} odstupa od izračunate {} za property {} — koristi se server-side iznos",
                    dto.getTotalPrice(), computedTotal, dto.getPropertyId());
        }
        reservation.setTotalPrice(computedTotal);

        Reservation saved = reservationRepository.save(reservation);

        // SAGA PATTERN: Emituj ReservationCreatedEvent.
        //   - property-service slusa → blokira termin na kalendaru
        //   - payment-service slusa  → pokrece naplatu (Task 3)
        //
        // RACE FIX: event se objavljuje tek NAKON COMMIT-a ove transakcije.
        // Saga zna zavrsiti za ~20ms — payment-service vrati PAYMENT_COMPLETED
        // prije nego sto je rezervacija upisana u bazu, listener je ne nadje
        // i ona ZAUVIJEK ostane CREATED (a wallet je naplacen). Zato su
        // rezervacije nasumicno "zapinjale" — pobjedjivao je brzi (commit ili saga).
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishCreatedEventSafely(saved);
                }
            });
        } else {
            // npr. unit testovi bez aktivne transakcije
            publishCreatedEventSafely(saved);
        }

        return reservationMapper.toResponseDTO(saved);
    }

    private void publishCreatedEventSafely(Reservation saved) {
        try {
            ReservationCreatedEvent event = new ReservationCreatedEvent(
                    saved.getId(),
                    saved.getPropertyId(),
                    saved.getGuestId(),
                    saved.getCheckIn().atStartOfDay(),
                    saved.getCheckOut().atStartOfDay(),
                    LocalDateTime.now(),
                    "RESERVATION_CREATED",
                    saved.getTotalPrice(),
                    "BAM",
                    saved.getHostId()  // BUG 2 — za host notifikaciju
            );
            reservationEventPublisher.publishReservationCreated(event);
        } catch (Exception e) {
            // Log but don't fail the reservation if event publishing fails
            // In production, use outbox pattern or transactional messaging
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn("⚠️ Event publishing failed for reservation {}: {}",
                            saved.getId(), e.getMessage());
        }
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
    public ReservationResponseDTO updateCancelStatus(Long id, Boolean isCancelled) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronadjena"));
        reservation.setIsCancelled(isCancelled);
        Reservation updated = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public ReservationResponseDTO cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rezervacija nije pronađena sa ID: " + id));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            // Vec otkazana — idempotent, ne emituj event ponovo
            return reservationMapper.toResponseDTO(reservation);
        }

        // Terminalni / aktivni statusi se ne smiju otkazivati
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Završena rezervacija ne može biti otkazana");
        }
        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Aktivna rezervacija (gost je već smješten) ne može biti otkazana — kontaktirajte support");
        }

        // Datum-based pravila se primjenjuju samo ako postoji check-in datum
        LocalDate today = LocalDate.now();
        int refundPct = 100; // default: full refund (npr. za CREATED status)
        if (reservation.getCheckIn() != null) {
            // Ako je check-in već prošao (ili je danas), refund nema smisla
            if (!reservation.getCheckIn().isAfter(today)) {
                throw new IllegalStateException(
                        "Datum dolaska (" + reservation.getCheckIn()
                                + ") je već prošao — rezervacija ne može biti otkazana");
            }

            // F6 — politika otkazivanja u 3 tier-a (po policy ili default):
            //   * unutar freeCancelDays prije check-ina → pun refund (100%)
            //   * van toga + noRefund=true → bez refunda (0%)
            //   * van toga + partialRefundPct postavljen → djelimican refund
            //   * inace → kao default (100% unutar 7 dana, 0% inace)
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                CancellationPolicy policy = reservation.getCancellationPolicy();
                int freeCancelDays = (policy != null && policy.getFreeCancelDays() != null)
                        ? policy.getFreeCancelDays() : 7;
                long daysUntilCheckIn = ChronoUnit.DAYS.between(today, reservation.getCheckIn());

                if (daysUntilCheckIn >= freeCancelDays) {
                    refundPct = 100;
                } else if (policy != null && Boolean.TRUE.equals(policy.getNoRefund())) {
                    refundPct = 0;
                } else if (policy != null && policy.getPartialRefundPct() != null) {
                    refundPct = policy.getPartialRefundPct();
                } else {
                    refundPct = 0; // default — bez politike, manje od freeCancelDays = nista
                }
            }
        }

        boolean wasConfirmed = reservation.getStatus() == ReservationStatus.CONFIRMED;
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);

        // SAGA kompenzacija — payment-service refundira wallet (ako je payment bio
        // COMPLETED), property-service oslobađa kalendar. Bez ovoga, cancel ostavlja
        // korisnika bez para i smjestaj ostaje "nedostupno".
        try {
            ReservationCancelledEvent event = new ReservationCancelledEvent(
                    saved.getId(),
                    saved.getPropertyId(),
                    saved.getGuestId(),
                    saved.getTotalPrice(),
                    "BAM",
                    wasConfirmed ? "Korisnik je otkazao potvrđenu rezervaciju (refund " + refundPct + "%)"
                                 : "Korisnik je otkazao rezervaciju",
                    LocalDateTime.now(),
                    "RESERVATION_CANCELLED",
                    saved.getHostId(),
                    refundPct
            );
            reservationEventPublisher.publishReservationCancelled(event);
            // NAPOMENA (K3 fix): ovdje se NE smije dirati Listing.isCancelled —
            // otkazivanje JEDNE rezervacije ne smije ugasiti listing cijelog
            // objekta. Kalendar se oslobađa automatski jer se CANCELLED
            // rezervacije izbacuju iz /occupied-dates liste.
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn("⚠️ Cancel event publish nije uspio za rezervaciju {}: {}",
                            saved.getId(), e.getMessage());
        }

        return reservationMapper.toResponseDTO(saved);
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
        // Task 5 — verify availability for every item in the batch BEFORE saving;
        // any single conflict aborts the whole batch (the @Transactional rollback
        // ensures partial state never leaks).
        for (ReservationRequestDTO dto : dtos) {
            propertyAvailabilityGateway.verifyAvailable(
                    dto.getPropertyId(), dto.getCheckIn(), dto.getCheckOut(), dto.getNumGuests());
        }

        List<Reservation> entities = dtos.stream().map(dto -> {
            Reservation r = reservationMapper.toEntity(dto);
            r.setStatus(ReservationStatus.CREATED);
            r.setCreatedAt(LocalDateTime.now());

            // K1 — ista server-side kalkulacija cijene kao u createReservation
            long nights = ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());
            PricingRuleDTO pricing = propertyAvailabilityGateway.fetchPricing(dto.getPropertyId());
            List<SeasonalRuleDTO> seasonalRules = propertyAvailabilityGateway.fetchSeasonalRules(dto.getPropertyId());
            BigDecimal total = priceCalculator.calculateTotal(
                    pricing, seasonalRules, dto.getCheckIn(), dto.getCheckOut());

            if (dto.getCancellationPolicyId() != null) {
                CancellationPolicy policy = cancellationPolicyRepository.findById(dto.getCancellationPolicyId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "CancellationPolicy nije pronađen sa ID: " + dto.getCancellationPolicyId()));
                if (policy.getPropertyId() != null && !policy.getPropertyId().equals(dto.getPropertyId())) {
                    throw new IllegalArgumentException("Politika otkazivanja ne pripada ovom smještaju");
                }
                r.setCancellationPolicy(policy);
            }
            if (dto.getPromoCodeId() != null) {
                PromoCode promo = promoCodeRepository.findById(dto.getPromoCodeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "PromoCode nije pronađen sa ID: " + dto.getPromoCodeId()));
                // K2 — ista validacija i evidencija koristenja kao kod pojedinacne rezervacije
                priceCalculator.validatePromo(promo, nights);
                total = priceCalculator.applyPromo(total, promo);
                promo.setUsageCount(promo.getUsageCount() + 1);
                promoCodeRepository.save(promo);
                r.setPromoCode(promo);
            }
            r.setTotalPrice(total);
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
