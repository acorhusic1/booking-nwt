package com.bookingnwt.notificationservice.config;

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
 * RabbitMQ konfiguracija za notification-service.
 *
 * Sluša payment saga evente i kreira notifikacije za korisnika:
 *   booking.payment.completed → "Rezervacija potvrđena"
 *   booking.payment.failed    → "Rezervacija otkazana"
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.notification}")
    private String notificationQueue;

    @Value("${app.rabbitmq.routing-key.payment-completed}")
    private String paymentCompletedKey;

    @Value("${app.rabbitmq.routing-key.payment-failed}")
    private String paymentFailedKey;

    // BUG 2 — notification-service sluša i RESERVATION_CREATED za "Nova rezervacija" hostu
    @Value("${app.rabbitmq.routing-key.reservation-created:booking.reservation.created}")
    private String reservationCreatedKey;

    // BUG A — notification-service sluša i RESERVATION_CANCELLED za "Otkazana rezervacija" hostu
    @Value("${app.rabbitmq.routing-key.reservation-cancelled:booking.reservation.cancelled}")
    private String reservationCancelledKey;

    // F9 — podsjetnik na dolazak + zahtjev za recenziju
    @Value("${app.rabbitmq.routing-key.reservation-reminder:booking.reservation.reminder}")
    private String reservationReminderKey;

    // F17 — prijava problema tokom boravka → notifikacija hostu
    @Value("${app.rabbitmq.routing-key.problem-reported:booking.problem.reported}")
    private String problemReportedKey;

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue notificationServiceQueue() {
        return new Queue(notificationQueue, true, false, false);
    }

    @Bean
    public Binding bindingPaymentCompleted(Queue notificationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationServiceQueue).to(bookingExchange).with(paymentCompletedKey);
    }

    @Bean
    public Binding bindingPaymentFailed(Queue notificationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationServiceQueue).to(bookingExchange).with(paymentFailedKey);
    }

    @Bean
    public Binding bindingReservationCreated(Queue notificationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationServiceQueue).to(bookingExchange).with(reservationCreatedKey);
    }

    @Bean
    public Binding bindingReservationCancelled(Queue notificationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationServiceQueue).to(bookingExchange).with(reservationCancelledKey);
    }

    @Bean
    public Binding bindingReservationReminder(Queue notificationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationServiceQueue).to(bookingExchange).with(reservationReminderKey);
    }

    @Bean
    public Binding bindingProblemReported(Queue notificationServiceQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationServiceQueue).to(bookingExchange).with(problemReportedKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
