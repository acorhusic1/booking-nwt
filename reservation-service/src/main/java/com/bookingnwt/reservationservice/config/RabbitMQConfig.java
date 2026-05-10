package com.bookingnwt.reservationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Konfiguracija za Reservation Service — Saga Pattern
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

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue reservationServiceQueue() {
        return new Queue(reservationQueue, true, false, false);
    }

    /**
     * Sluša PropertyReserved event od Property Service-a — potvrda rezervacije
     */
    @Bean
    public Binding bindingPropertyReserved(Queue reservationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(reservationServiceQueue)
                .to(bookingExchange)
                .with(propertyReservedRoutingKey);
    }

    /**
     * Sluša Compensation event — rollback rezervacije
     */
    @Bean
    public Binding bindingCompensation(Queue reservationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(reservationServiceQueue)
                .to(bookingExchange)
                .with(compensationRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
