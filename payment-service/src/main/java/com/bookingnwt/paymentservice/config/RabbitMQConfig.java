package com.bookingnwt.paymentservice.config;

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
 * RabbitMQ konfiguracija za payment-service — Saga Pattern.
 *
 * Payment-service je drugi participant u Saga-i:
 *   1. SLUŠA  routing key: booking.reservation.created (od reservation-service)
 *   2. EMITUJE routing key: booking.payment.completed  (uspjeh)
 *      ili      booking.payment.failed     (kompenzacija)
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.payment}")
    private String paymentQueue;

    @Value("${app.rabbitmq.queue.payment-cancellations}")
    private String paymentCancellationsQueue;

    @Value("${app.rabbitmq.queue.payment-completions:payment.completions.queue}")
    private String paymentCompletionsQueue;

    @Value("${app.rabbitmq.routing-key.reservation-created}")
    private String reservationCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.reservation-cancelled}")
    private String reservationCancelledRoutingKey;

    @Value("${app.rabbitmq.routing-key.reservation-completed:booking.reservation.completed}")
    private String reservationCompletedRoutingKey;

    // ============ EXCHANGE ============
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    // ============ QUEUES ============
    @Bean
    public Queue paymentServiceQueue() {
        return new Queue(paymentQueue, true, false, false);
    }

    /**
     * Odvojeni queue za cancellation event-e da se ne miješaju sa glavnim
     * payment flow-om i da imaju vlastiti retry/DLQ ako zatreba.
     */
    @Bean
    public Queue paymentCancellationsQueue() {
        return new Queue(paymentCancellationsQueue, true, false, false);
    }

    /**
     * F19 — odvojeni queue za "boravak zavrsen" evente: tek tada se host
     * isplacuje (umanjeno za proviziju platforme).
     */
    @Bean
    public Queue paymentCompletionsQueue() {
        return new Queue(paymentCompletionsQueue, true, false, false);
    }

    // ============ BINDINGS ============
    /** Sluša ReservationCreated event — naplati rezervaciju. */
    @Bean
    public Binding bindingReservationCreated(Queue paymentServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(paymentServiceQueue)
                .to(bookingExchange)
                .with(reservationCreatedRoutingKey);
    }

    /** Sluša ReservationCompleted event — isplati hosta (F19). */
    @Bean
    public Binding bindingReservationCompleted(Queue paymentCompletionsQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(paymentCompletionsQueue)
                .to(bookingExchange)
                .with(reservationCompletedRoutingKey);
    }

    /** Sluša ReservationCancelled event — refundira wallet (kompenzacija). */
    @Bean
    public Binding bindingReservationCancelled(Queue paymentCancellationsQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(paymentCancellationsQueue)
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
