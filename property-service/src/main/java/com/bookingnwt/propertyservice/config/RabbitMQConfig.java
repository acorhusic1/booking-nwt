package com.bookingnwt.propertyservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Konfiguracija za Saga Pattern
 * Definiše exchange, queue-ove i bindings za event-driven komunikaciju
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.property}")
    private String propertyQueue;

    @Value("${app.rabbitmq.routing-key.reserved}")
    private String reservedRoutingKey;

    @Value("${app.rabbitmq.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    @Value("${app.rabbitmq.routing-key.compensation}")
    private String compensationRoutingKey;

    @Value("${app.rabbitmq.routing-key.reservation-created:booking.reservation.created}")
    private String reservationCreatedRoutingKey;

    // ============ EXCHANGE ============
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    // ============ QUEUE ============
    @Bean
    public Queue propertyServiceQueue() {
        return new Queue(propertyQueue, true, false, false);
    }

    // ============ BINDINGS ============
    @Bean
    public Binding bindingReservedEvent(Queue propertyServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyServiceQueue)
                .to(bookingExchange)
                .with(reservedRoutingKey);
    }

    @Bean
    public Binding bindingPaymentFailed(Queue propertyServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyServiceQueue)
                .to(bookingExchange)
                .with(paymentFailedRoutingKey);
    }

    @Bean
    public Binding bindingCompensation(Queue propertyServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyServiceQueue)
                .to(bookingExchange)
                .with(compensationRoutingKey);
    }

    @Bean
    public Binding bindingReservationCreated(Queue propertyServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(propertyServiceQueue)
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
