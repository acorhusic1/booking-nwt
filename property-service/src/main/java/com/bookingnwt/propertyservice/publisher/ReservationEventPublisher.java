package com.bookingnwt.propertyservice.publisher;

import com.bookingnwt.propertyservice.events.PropertyReservedEvent;
import com.bookingnwt.propertyservice.events.ReservationCompensationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Publisher za Saga Pattern Event-e
 * Odgovoran za emitovanje event-a u RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.reserved}")
    private String reservedRoutingKey;

    @Value("${app.rabbitmq.routing-key.compensation}")
    private String compensationRoutingKey;

    /**
     * Emituje PropertyReservedEvent
     * Signalizira da je znanje uspješno rezervirano
     */
    public void publishPropertyReserved(PropertyReservedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, reservedRoutingKey, message);
            log.info("📤 ✅ PropertyReservedEvent objavljen: Property={}, Reservation={}",
                    event.getPropertyId(), event.getReservationId());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju PropertyReservedEvent", e);
            throw new RuntimeException("Failed to publish PropertyReservedEvent", e);
        }
    }

    /**
     * Emituje ReservationCompensationEvent
     * Signalizira kompenzaciju - trebam vratiti sistem u inicijalno stanje
     */
    public void publishReservationCompensation(ReservationCompensationEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, compensationRoutingKey, message);
            log.info("⏮️ ✅ ReservationCompensationEvent objavljen: Reservation={}, Razlog={}",
                    event.getReservationId(), event.getReason());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju kompenzacije", e);
            throw new RuntimeException("Failed to publish compensation event", e);
        }
    }
}

