package com.bookingnwt.propertyservice.listener;

import com.bookingnwt.propertyservice.events.PaymentFailedEvent;
import com.bookingnwt.propertyservice.events.ReservationCreatedEvent;
import com.bookingnwt.propertyservice.events.ReservationCompensationEvent;
import com.bookingnwt.propertyservice.events.PropertyReservedEvent;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.publisher.ReservationEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SAGA PATTERN — Property Service Listener
 *
 * Jedan listener na jednom queue-u koji detektuje tip eventa
 * i rutira obradu na odgovarajuću metodu.
 *
 * Flow:
 * 1. Reservation Service kreira rezervaciju → emituje RESERVATION_CREATED
 * 2. Property Service sluša → markira property kao nedostupan → emituje PROPERTY_RESERVED
 * 3. Ako Payment padne → prima PAYMENT_FAILED → KOMPENZACIJA: oslobodi property
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final PropertyRepository propertyRepository;
    private final ReservationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * JEDAN listener na queue-u koji detektuje tip eventa.
     * Razlikuje event tipove po prisustvu polja u JSON-u.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.property}")
    @Transactional
    public void handleMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            log.info("📨 Primljena poruka na property.service.queue");

            // Detekcija tipa eventa
            if (jsonNode.has("eventType")) {
                String eventType = jsonNode.get("eventType").asText();
                switch (eventType) {
                    case "RESERVATION_CREATED":
                        handleReservationCreated(message);
                        break;
                    case "PAYMENT_FAILED":
                        handlePaymentFailed(message);
                        break;
                    default:
                        log.warn("⚠️ Nepoznat eventType: {}", eventType);
                }
            } else if (jsonNode.has("reason") && jsonNode.has("propertyId")) {
                // Fallback: PaymentFailedEvent nema eventType, ali ima reason
                handlePaymentFailed(message);
            } else if (jsonNode.has("reservationId") && jsonNode.has("propertyId")) {
                // Fallback: ReservationCreated
                handleReservationCreated(message);
            } else {
                log.warn("⚠️ Neprepoznata poruka: {}", message);
            }
        } catch (Exception e) {
            log.error("❌ Greška pri parsiranju poruke: {}", e.getMessage(), e);
        }
    }

    /**
     * SAGA KORAK 2: Reservation Service je kreirao rezervaciju.
     * Property Service treba markirati nekretninu kao nedostupnu.
     *
     * LOKALNA TRANSAKCIJA: available = false
     * Zatim emituje PROPERTY_RESERVED event prema Reservation Service-u.
     */
    private void handleReservationCreated(String message) {
        try {
            ReservationCreatedEvent event = objectMapper.readValue(message, ReservationCreatedEvent.class);
            log.info("📨 ReservationCreatedEvent primljen: Reservation={}, Property={}",
                    event.getReservationId(), event.getPropertyId());

            Optional<Property> propertyOpt = propertyRepository.findById(event.getPropertyId());

            if (propertyOpt.isPresent()) {
                Property property = propertyOpt.get();

                // LOKALNA TRANSAKCIJA: Markiranje nekretnine kao nedostupne
                property.setAvailable(false);
                propertyRepository.save(property);
                log.info("✅ Nekretnina {} je markirana kao NEDOSTUPNA za rezervaciju {}",
                        event.getPropertyId(), event.getReservationId());

                // Emituj PROPERTY_RESERVED event → Reservation Service sluša
                PropertyReservedEvent reservedEvent = new PropertyReservedEvent(
                        event.getPropertyId(),
                        event.getReservationId(),
                        event.getUserId(),
                        1,
                        event.getCheckInDate(),
                        event.getCheckOutDate(),
                        LocalDateTime.now(),
                        "CONFIRMED"
                );
                eventPublisher.publishPropertyReserved(reservedEvent);

            } else {
                log.warn("⚠️ Nekretnina sa ID {} nije pronađena — emitujem kompenzaciju", event.getPropertyId());

                // Property ne postoji — emituj kompenzaciju da se otkaže rezervacija
                ReservationCompensationEvent compensationEvent = new ReservationCompensationEvent(
                        event.getReservationId(),
                        event.getPropertyId(),
                        "Property not found - reservation must be cancelled",
                        true
                );
                eventPublisher.publishReservationCompensation(compensationEvent);
            }

        } catch (Exception e) {
            log.error("❌ Greška u handleReservationCreated: {}", e.getMessage(), e);
        }
    }

    /**
     * SAGA KOMPENZACIJA: Plaćanje je palo.
     * Trebamo osloboditi nekretninu — vratiti available = true.
     *
     * INVERZNA AKCIJA: Poništavamo promjenu iz handleReservationCreated
     */
    private void handlePaymentFailed(String message) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
            log.info("📨 ⚠️ PaymentFailedEvent primljen: Reservation={}, Reason={}",
                    event.getReservationId(), event.getReason());

            Optional<Property> propertyOpt = propertyRepository.findById(event.getPropertyId());

            if (propertyOpt.isPresent()) {
                Property property = propertyOpt.get();

                // KOMPENZACIONA AKCIJA: Oslobodi nekretninu
                property.setAvailable(true);
                propertyRepository.save(property);
                log.info("✅ KOMPENZACIJA: Nekretnina {} je oslobođena jer je plaćanje neuspješno",
                        event.getPropertyId());

                // Emituj kompenzacioni event za Reservation Service
                ReservationCompensationEvent compensationEvent = new ReservationCompensationEvent(
                        event.getReservationId(),
                        event.getPropertyId(),
                        "Payment failed - property released: " + event.getReason(),
                        true
                );
                eventPublisher.publishReservationCompensation(compensationEvent);

            } else {
                log.warn("⚠️ Nekretnina sa ID {} nije pronađena za kompenzaciju", event.getPropertyId());
            }

        } catch (Exception e) {
            log.error("❌ Greška u handlePaymentFailed: {}", e.getMessage(), e);
        }
    }
}
