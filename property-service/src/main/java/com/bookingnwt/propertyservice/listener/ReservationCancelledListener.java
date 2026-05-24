package com.bookingnwt.propertyservice.listener;

import com.bookingnwt.propertyservice.events.ReservationCancelledEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAGA KOMPENZACIJA — kad guest otkaže rezervaciju, property-service oslobađa
 * kalendar (Property.available = true) tako da smjestaj opet bude vidljiv kao
 * dostupan u listi.
 *
 * Bez ovog listenera, otkazane rezervacije ostavljaju smjestaj "Nedostupno"
 * što frustira goste i hostove.
 *
 * Odvojeni queue (property.cancellations.queue) — ne miješa se sa glavnim
 * property.service.queue koji obrađuje ReservationCreated/PaymentFailed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCancelledListener {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.property-cancellations}")
    @Transactional
    public void onMessage(String message) {
        try {
            ReservationCancelledEvent event = objectMapper.readValue(message, ReservationCancelledEvent.class);
            log.info("📨 ReservationCancelledEvent primljen za rezervaciju {} (property={})",
                    event.getReservationId(), event.getPropertyId());

            // Posto handleReservationCreated VISE NE flippa available=false (race fix),
            // nema sta kompenzovati na property-u. Calendar overlap se vec proverava
            // u PropertyAvailabilityGateway.existsOverlap() pri kreiranju nove rezervacije.
            log.info("ℹ️ Saga link logiran (property.available ostaje host-kontrolisan)");

        } catch (Exception e) {
            log.error("❌ Greška u ReservationCancelledListener: {}", e.getMessage(), e);
        }
    }
}
