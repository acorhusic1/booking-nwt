package com.bookingnwt.paymentservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Odgađa start RabbitMQ consumer-a do ApplicationReadyEvent-a kako bi
 * DataLoader (CommandLineRunner) imao priliku da seedu-je DB prije nego
 * što listener počne čitati zaglavljene poruke iz queue-a.
 *
 * Bez ovoga, race condition: listener pokušava DB write (npr. update
 * wallet-a) prije nego što DataLoader insert-uje seed podatke, pa Saga
 * završava CANCELLED iako je trebao COMPLETED.
 *
 * Spring Boot inače auto-startuje listener container-e na context
 * refresh — ovde to ručno radimo na app-ready event.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rabbit.auto-start-listeners", havingValue = "true", matchIfMissing = true)
public class RabbitListenerStarter {

    private final RabbitListenerEndpointRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void startListeners() {
        registry.getListenerContainers().forEach(container -> {
            if (!container.isRunning()) {
                container.start();
                log.info("RabbitMQ listener container started after app ready");
            }
        });
    }
}
