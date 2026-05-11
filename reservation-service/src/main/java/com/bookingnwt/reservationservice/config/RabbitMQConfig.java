package com.bookingnwt.reservationservice.config;

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
 * RabbitMQ Konfiguracija za reservation-service — Saga Pattern (choreography).
 *
 * reservation.service.queue prima event-e od:
 *   - property-service: booking.property.reserved, booking.reservation.compensation
 *   - payment-service:  booking.payment.completed, booking.payment.failed (Task 3)
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.reservation}")
    private String reservationQueue;

    @Value("${app.rabbitmq.routing-key.property-reserved}")
    private String propertyReservedRoutingKey;

    @Value("${app.rabbitmq.routing-key.compensation}")
    private String compensationRoutingKey;

    @Value("${app.rabbitmq.routing-key.payment-completed}")
    private String paymentCompletedRoutingKey;

    @Value("${app.rabbitmq.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue reservationServiceQueue() {
        return new Queue(reservationQueue, true, false, false);
    }

    @Bean
    public Binding bindingPropertyReserved(Queue reservationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(reservationServiceQueue).to(bookingExchange).with(propertyReservedRoutingKey);
    }

    @Bean
    public Binding bindingCompensation(Queue reservationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(reservationServiceQueue).to(bookingExchange).with(compensationRoutingKey);
    }

    /** Task 3 — Saga: payment uspjeh */
    @Bean
    public Binding bindingPaymentCompleted(Queue reservationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(reservationServiceQueue).to(bookingExchange).with(paymentCompletedRoutingKey);
    }

    /** Task 3 — Saga: payment kompenzacija */
    @Bean
    public Binding bindingPaymentFailed(Queue reservationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(reservationServiceQueue).to(bookingExchange).with(paymentFailedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
