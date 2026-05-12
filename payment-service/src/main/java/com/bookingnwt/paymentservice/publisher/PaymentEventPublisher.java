package com.bookingnwt.paymentservice.publisher;

import com.bookingnwt.paymentservice.events.PaymentCompletedEvent;
import com.bookingnwt.paymentservice.events.PaymentFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SAGA PATTERN — Payment Service Publisher.
 *
 * Emituje rezultat naplate prema reservation-service-u:
 *   - PaymentCompletedEvent → booking.payment.completed   (uspjeh, FINAL)
 *   - PaymentFailedEvent    → booking.payment.failed      (kompenzacija)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.payment-completed}")
    private String paymentCompletedRoutingKey;

    @Value("${app.rabbitmq.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, paymentCompletedRoutingKey, message);
            log.info("📤 ✅ PaymentCompletedEvent objavljen: Payment={}, Reservation={}",
                    event.getPaymentId(), event.getReservationId());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju PaymentCompletedEvent", e);
            throw new RuntimeException("Failed to publish PaymentCompletedEvent", e);
        }
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, paymentFailedRoutingKey, message);
            log.info("📤 ⚠️ PaymentFailedEvent objavljen: Payment={}, Reservation={}, Razlog={}",
                    event.getPaymentId(), event.getReservationId(), event.getReason());
        } catch (Exception e) {
            log.error("❌ Greška pri objavljivanju PaymentFailedEvent", e);
            throw new RuntimeException("Failed to publish PaymentFailedEvent", e);
        }
    }
}
