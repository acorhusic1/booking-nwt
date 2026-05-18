package com.bookingnwt.propertyservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ konfiguracija za property-service — Saga Pattern (choreography).
 *
 * property.service.queue prima samo event-e koje DRUGI servisi emituju:
 *   - booking.reservation.created  (od reservation-service-a)  → blokiraj kalendar
 *   - booking.payment.failed       (od payment-service-a)      → oslobodi kalendar (kompenzacija)
 *
 * NE smije biti bound na svoje izlazne routing-key-eve
 * (`booking.property.reserved`, `booking.reservation.compensation`) — to bi
 * uzrokovalo da property-service prima sopstvene poruke i pravi infinite loop
 * (vidjeli smo 399 poruka/s u RabbitMQ Management UI-ju).
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.property}")
    private String propertyQueue;

    @Value("${app.rabbitmq.queue.property-cancellations:property.cancellations.queue}")
    private String propertyCancellationsQueue;

    @Value("${app.rabbitmq.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    @Value("${app.rabbitmq.routing-key.reservation-created:booking.reservation.created}")
    private String reservationCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.reservation-cancelled:booking.reservation.cancelled}")
    private String reservationCancelledRoutingKey;

    // ============ EXCHANGE ============
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    // ============ QUEUES ============
    @Bean
    public Queue propertyServiceQueue() {
        return new Queue(propertyQueue, true, false, false);
    }

    @Bean
    public Queue propertyCancellationsQueue() {
        return new Queue(propertyCancellationsQueue, true, false, false);
    }

    // ============ BINDINGS ============
    /** Glavni ulazni event — okidač Sage. */
    @Bean
    public Binding bindingReservationCreated(Queue propertyServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyServiceQueue)
                .to(bookingExchange)
                .with(reservationCreatedRoutingKey);
    }

    /** Kompenzacija — payment je propao, treba osloboditi kalendar. */
    @Bean
    public Binding bindingPaymentFailed(Queue propertyServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyServiceQueue)
                .to(bookingExchange)
                .with(paymentFailedRoutingKey);
    }

    /** Cancel saga — kad guest otkaže, oslobodi property. */
    @Bean
    public Binding bindingReservationCancelled(Queue propertyCancellationsQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyCancellationsQueue)
                .to(bookingExchange)
                .with(reservationCancelledRoutingKey);
    }

    // ============ MESSAGE CONVERTER ============
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
