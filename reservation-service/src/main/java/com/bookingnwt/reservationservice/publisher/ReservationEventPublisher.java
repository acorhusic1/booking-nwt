package com.bookingnwt.reservationservice.publisher;

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

    /**
     * Emituje ReservationCreatedEvent u RabbitMQ.
     * Property Service sluša i markira nekretninu kao nedostupnu.
     */
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
