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

    @Value("${app.rabbitmq.routing-key.reservation-created}")
    private String reservationCreatedRoutingKey;

    // ============ EXCHANGE ============
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    // ============ QUEUE ============
    @Bean
    public Queue paymentServiceQueue() {
        return new Queue(paymentQueue, true, false, false);
    }

    // ============ BINDINGS ============
    /**
     * Sluša ReservationCreated event — naplati rezervaciju.
     */
    @Bean
    public Binding bindingReservationCreated(Queue paymentServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(paymentServiceQueue)
                .to(bookingExchange)
                .with(reservationCreatedRoutingKey);
    }

    // ============ MESSAGE CONVERTER ============
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
