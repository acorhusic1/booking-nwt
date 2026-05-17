package com.bookingnwt.propertyservice.listener;

import com.bookingnwt.propertyservice.events.ReservationCancelledEvent;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    private final PropertyRepository propertyRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.property-cancellations}")
    @Transactional
    public void onMessage(String message) {
        try {
            ReservationCancelledEvent event = objectMapper.readValue(message, ReservationCancelledEvent.class);
            log.info("📨 ReservationCancelledEvent primljen za rezervaciju {} (property={})",
                    event.getReservationId(), event.getPropertyId());

            if (event.getPropertyId() == null) {
                log.warn("⚠️ Cancel event bez propertyId — preskačem");
                return;
            }

            Optional<Property> opt = propertyRepository.findById(event.getPropertyId());
            if (opt.isEmpty()) {
                log.warn("⚠️ Property {} nije pronađen za oslobađanje", event.getPropertyId());
                return;
            }

            Property property = opt.get();
            if (Boolean.TRUE.equals(property.getAvailable())) {
                log.info("ℹ️ Property {} je već dostupan — preskačem", property.getId());
                return;
            }
            property.setAvailable(true);
            propertyRepository.save(property);
            log.info("✅ KOMPENZACIJA: Property {} oslobođen (available=true) nakon cancel rezervacije {}",
                    property.getId(), event.getReservationId());

        } catch (Exception e) {
            log.error("❌ Greška u ReservationCancelledListener: {}", e.getMessage(), e);
        }
    }
}
