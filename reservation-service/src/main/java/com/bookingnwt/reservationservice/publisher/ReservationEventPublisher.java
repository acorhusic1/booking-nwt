package com.bookingnwt.reservationservice.publisher;

import com.bookingnwt.reservationservice.events.ReservationCancelledEvent;
import com.bookingnwt.reservationservice.events.ReservationCreatedEvent;
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
