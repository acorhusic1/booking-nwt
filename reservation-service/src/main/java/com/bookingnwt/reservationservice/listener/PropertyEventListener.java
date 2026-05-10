package com.bookingnwt.reservationservice.listener;

import com.bookingnwt.reservationservice.events.PropertyReservedEvent;
import com.bookingnwt.reservationservice.events.ReservationCompensationEvent;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * SAGA PATTERN — Reservation Service Listener
 *
 * Sluša event-e od Property Service-a:
 * 1. PropertyReservedEvent → potvrda da je nekretnina rezervirana → status CONFIRMED
 * 2. ReservationCompensationEvent → rollback → status CANCELLED
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyEventListener {

    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.reservation}")
    @Transactional
    public void handleMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            log.info("📨 Primljena poruka na reservation.service.queue");

            if (jsonNode.has("compensated")) {
                handleCompensation(message);
            } else if (jsonNode.has("status") && jsonNode.has("propertyId")) {
                handlePropertyReserved(message);
            } else {
                log.warn("⚠️ Neprepoznata poruka: {}", message);
            }
        } catch (Exception e) {
            log.error("❌ Greška pri parsiranju poruke: {}", e.getMessage(), e);
        }
    }

    /**
     * SAGA KORAK 3: Property Service je potvrdio rezervaciju.
     * Ažuriraj status rezervacije na CONFIRMED.
     */
    private void handlePropertyReserved(String message) {
        try {
            PropertyReservedEvent event = objectMapper.readValue(message, PropertyReservedEvent.class);
            log.info("📨 PropertyReservedEvent primljen: Reservation={}, Status={}",
                    event.getReservationId(), event.getStatus());

            Optional<Reservation> reservationOpt = reservationRepository.findById(event.getReservationId());

            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();

                if ("CONFIRMED".equals(event.getStatus())) {
                    reservation.setStatus(ReservationStatus.CONFIRMED);
                    reservation.setUpdatedAt(java.time.LocalDateTime.now());
                    reservationRepository.save(reservation);
                    log.info("✅ ═══════════════════════════════════════════════════════");
                    log.info("✅ SAGA COMPLETED — Rezervacija {} je POTVRĐENA", event.getReservationId());
                    log.info("✅ Lokalna transakcija 1 (Reservation): CREATED → CONFIRMED");
                    log.info("✅ Lokalna transakcija 2 (Property): available = false");
                    log.info("✅ Obje transakcije uspješne — akcija je FINALNA");
                    log.info("✅ ═══════════════════════════════════════════════════════");
                } else {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                    reservation.setUpdatedAt(java.time.LocalDateTime.now());
                    reservationRepository.save(reservation);
                    log.warn("⚠️ Rezervacija {} je CANCELLED", event.getReservationId());
                }
            } else {
                log.warn("⚠️ Rezervacija {} nije pronađena", event.getReservationId());
            }

        } catch (Exception e) {
            log.error("❌ Greška u handlePropertyReserved: {}", e.getMessage(), e);
        }
    }

    /**
     * SAGA KOMPENZACIJA: Property Service je poslao rollback event.
     * Otkazujemo rezervaciju — status na CANCELLED.
     *
     * INVERZNA AKCIJA od kreiranja rezervacije
     */
    private void handleCompensation(String message) {
        try {
            ReservationCompensationEvent event = objectMapper.readValue(message, ReservationCompensationEvent.class);
            log.info("📨 ⚠️ ReservationCompensationEvent primljen: Reservation={}, Reason={}",
                    event.getReservationId(), event.getReason());

            Optional<Reservation> reservationOpt = reservationRepository.findById(event.getReservationId());

            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                log.info("🔙 KOMPENZACIJA: Rezervacija {} je OTKAZANA — Razlog: {}",
                        event.getReservationId(), event.getReason());
            } else {
                log.warn("⚠️ Rezervacija {} nije pronađena za kompenzaciju", event.getReservationId());
            }

        } catch (Exception e) {
            log.error("❌ Greška u handleCompensation: {}", e.getMessage(), e);
        }
    }
}
