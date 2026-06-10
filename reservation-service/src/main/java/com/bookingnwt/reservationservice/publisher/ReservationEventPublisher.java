package com.bookingnwt.reservationservice.publisher;

import com.bookingnwt.reservationservice.events.ProblemReportedEvent;
import com.bookingnwt.reservationservice.events.ReservationCancelledEvent;
import com.bookingnwt.reservationservice.events.ReservationCompletedEvent;
import com.bookingnwt.reservationservice.events.ReservationCreatedEvent;
import com.bookingnwt.reservationservice.events.ReservationReminderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SAGA PATTERN — Reservation Service Publisher
 * 
 * Emituje ReservationCreatedEvent kada se kreira nova rezervacija.
 * Property Service sluša ovaj event i markira nekretninu kao nedostupnu.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.reservation-created}")
    private String reservationCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.reservation-cancelled}")
    private String reservationCancelledRoutingKey;

    // F9 — podsjetnik na dolazak / zahtjev za recenziju (notifikacije, ne saga)
    @Value("${app.rabbitmq.routing-key.reservation-reminder:booking.reservation.reminder}")
    private String reservationReminderRoutingKey;

    // F19 — boravak zavrsen → payment-service isplacuje hosta
    @Value("${app.rabbitmq.routing-key.reservation-completed:booking.reservation.completed}")
    private String reservationCompletedRoutingKey;

    // F17 — prijava problema → notifikacija hostu
    @Value("${app.rabbitmq.routing-key.problem-reported:booking.problem.reported}")
    private String problemReportedRoutingKey;

    /**
     * Emituje ReservationCreatedEvent u RabbitMQ.
     * Property Service sluša i markira nekretninu kao nedostupnu.
     */
    /**
     * Emituje ReservationCancelledEvent kad korisnik otkaže rezervaciju.
     * Payment-service prima → refund. Property-service prima → release calendar.
     */
    public void publishReservationCancelled(ReservationCancelledEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, reservationCancelledRoutingKey, message);
            log.info("📤 ⏪ ReservationCancelledEvent objavljen: Reservation={}, iznos za refund={} {}",
                    event.getReservationId(), event.getTotalPrice(), event.getCurrency());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju ReservationCancelledEvent", e);
            throw new RuntimeException("Failed to publish ReservationCancelledEvent", e);
        }
    }

    public void publishReservationReminder(ReservationReminderEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, reservationReminderRoutingKey, message);
            log.info("📤 🔔 ReservationReminder objavljen: type={}, reservation={}, guest={}",
                    event.getEventType(), event.getReservationId(), event.getGuestId());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju ReservationReminderEvent", e);
        }
    }

    /**
     * F19 — emituje se kad scheduler prebaci rezervaciju u COMPLETED.
     * payment-service slusa i isplacuje hosta (minus provizija platforme).
     */
    public void publishReservationCompleted(ReservationCompletedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, reservationCompletedRoutingKey, message);
            log.info("📤 🏁 ReservationCompletedEvent objavljen: Reservation={}, host payout {} {}",
                    event.getReservationId(), event.getTotalPrice(), event.getCurrency());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju ReservationCompletedEvent", e);
        }
    }

    /** F17 — prijava problema tokom boravka → notifikacija hostu. */
    public void publishProblemReported(ProblemReportedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, problemReportedRoutingKey, message);
            log.info("📤 🛠 ProblemReportedEvent objavljen: Report={}, Reservation={}, Host={}",
                    event.getReportId(), event.getReservationId(), event.getHostId());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju ProblemReportedEvent", e);
        }
    }

    public void publishReservationCreated(ReservationCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, reservationCreatedRoutingKey, message);
            log.info("📤 ✅ ReservationCreatedEvent objavljen: Reservation={}, Property={}",
                    event.getReservationId(), event.getPropertyId());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju ReservationCreatedEvent", e);
            throw new RuntimeException("Failed to publish ReservationCreatedEvent", e);
        }
    }
}
