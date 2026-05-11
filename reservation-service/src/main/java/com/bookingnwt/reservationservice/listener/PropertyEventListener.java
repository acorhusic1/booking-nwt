package com.bookingnwt.reservationservice.listener;

import com.bookingnwt.reservationservice.events.PaymentCompletedEvent;
import com.bookingnwt.reservationservice.events.PaymentFailedEvent;
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
 * SAGA PATTERN — reservation-service listener.
 *
 * Sluša sve event-e na reservation.service.queue i grana po eventType polju
 * (jednostavnije od više listener metoda — sve poruke dolaze na isti queue).
 *
 * Routing keys koje obrađujemo:
 *   - PROPERTY_RESERVED        → status=CONFIRMED  (potvrda od property-a)
 *   - RESERVATION_COMPENSATION → status=CANCELLED  (rollback od property-a)
 *   - PAYMENT_COMPLETED        → status=CONFIRMED  (uspjeh naplate, FINAL — Task 3)
 *   - PAYMENT_FAILED           → status=CANCELLED  (kompenzacija — Task 3)
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
            JsonNode node = objectMapper.readTree(message);
            String type = node.has("eventType") ? node.get("eventType").asText() : "";

            switch (type) {
                case "PAYMENT_COMPLETED" -> handlePaymentCompleted(message);
                case "PAYMENT_FAILED"    -> handlePaymentFailed(message);
                default -> {
                    // Stari property event-i — postojeci heuristic
                    if (node.has("compensated")) {
                        handleCompensation(message);
                    } else if (node.has("status") && node.has("propertyId")) {
                        handlePropertyReserved(message);
                    } else {
                        log.warn("Neprepoznata poruka na reservation queue: {}", message);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Greška pri obradi poruke: {}", e.getMessage(), e);
        }
    }

    /* ========================================================
     *  Task 3 — Payment Saga listeneri
     * ======================================================== */

    /** SAGA FINAL: payment je prošao → potvrdi rezervaciju. */
    private void handlePaymentCompleted(String message) throws Exception {
        PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
        log.info("📨 PaymentCompletedEvent: Payment={}, Reservation={}, iznos={} {}",
                event.getPaymentId(), event.getReservationId(),
                event.getAmount(), event.getCurrency());

        updateReservationStatus(event.getReservationId(), ReservationStatus.CONFIRMED,
                "✅ SAGA FINAL — rezervacija {} POTVRĐENA (payment {} OK)",
                event.getReservationId(), event.getPaymentId());
    }

    /** SAGA KOMPENZACIJA: payment je propao → otkaži rezervaciju (inverzna akcija). */
    private void handlePaymentFailed(String message) throws Exception {
        PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
        log.warn("📨 PaymentFailedEvent: Payment={}, Reservation={}, Razlog={}",
                event.getPaymentId(), event.getReservationId(), event.getReason());

        updateReservationStatus(event.getReservationId(), ReservationStatus.CANCELLED,
                "🔙 SAGA KOMPENZACIJA — rezervacija {} OTKAZANA (payment failed: {})",
                event.getReservationId(), event.getReason());
    }

    /* ========================================================
     *  Postojeći Property Saga listeneri (Emir)
     * ======================================================== */

    private void handlePropertyReserved(String message) {
        try {
            PropertyReservedEvent event = objectMapper.readValue(message, PropertyReservedEvent.class);
            log.info("PropertyReservedEvent: Reservation={}, Status={}",
                    event.getReservationId(), event.getStatus());

            Optional<Reservation> reservationOpt = reservationRepository.findById(event.getReservationId());

            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();

                if ("CONFIRMED".equals(event.getStatus())) {
                    reservation.setStatus(ReservationStatus.CONFIRMED);
                    reservation.setUpdatedAt(java.time.LocalDateTime.now());
                    reservationRepository.save(reservation);
                    log.info("═══════════════════════════════════════════════════════");
                    log.info("SAGA COMPLETED — Rezervacija {} je POTVRĐENA", event.getReservationId());
                    log.info("Lokalna transakcija 1 (Reservation): CREATED → CONFIRMED");
                    log.info("Lokalna transakcija 2 (Property): available = false");
                    log.info("Obje transakcije uspješne — akcija je FINALNA");
                    log.info("═══════════════════════════════════════════════════════");
                } else {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                    reservation.setUpdatedAt(java.time.LocalDateTime.now());
                    reservationRepository.save(reservation);
                    log.warn("Rezervacija {} je CANCELLED", event.getReservationId());
                }
            } else {
                log.warn("Rezervacija {} nije pronađena", event.getReservationId());
            }

        } catch (Exception e) {
            log.error("Greška u handlePropertyReserved: {}", e.getMessage(), e);
        }
    }

    private void handleCompensation(String message) {
        try {
            ReservationCompensationEvent event = objectMapper.readValue(message, ReservationCompensationEvent.class);
            log.info("ReservationCompensationEvent: Reservation={}, Reason={}",
                    event.getReservationId(), event.getReason());

            updateReservationStatus(event.getReservationId(), ReservationStatus.CANCELLED,
                    "KOMPENZACIJA: Rezervacija {} OTKAZANA (razlog: {})",
                    event.getReservationId(), event.getReason());
        } catch (Exception e) {
            log.error("Greška u handleCompensation: {}", e.getMessage(), e);
        }
    }

    /* ========================================================
     *  Helper
     * ======================================================== */

    /**
     * Saga state-machine: CANCELLED je TERMINAL. Bilo koji success event
     * (CONFIRMED) koji stigne nakon kompenzacije se ignoriše. Tako rješavamo
     * race condition između property-saga i payment-saga — ako bilo koji
     * od njih kaže "fail", finalni status ostaje CANCELLED bez obzira što
     * drugi kaže "success".
     */
    private void updateReservationStatus(Long reservationId, ReservationStatus status,
                                          String logFormat, Object... logArgs) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) {
            log.warn("Rezervacija {} nije pronađena", reservationId);
            return;
        }
        Reservation reservation = opt.get();

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                && status == ReservationStatus.CONFIRMED) {
            log.warn("Ignorišem CONFIRMED za rezervaciju {} — već je CANCELLED (kompenzacija je terminal)",
                    reservationId);
            return;
        }

        reservation.setStatus(status);
        reservationRepository.save(reservation);
        log.info(logFormat, logArgs);
    }
}
