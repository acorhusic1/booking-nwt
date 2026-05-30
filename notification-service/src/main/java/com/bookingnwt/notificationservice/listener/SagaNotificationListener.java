package com.bookingnwt.notificationservice.listener;

import com.bookingnwt.notificationservice.events.PaymentCompletedEvent;
import com.bookingnwt.notificationservice.events.PaymentFailedEvent;
import com.bookingnwt.notificationservice.events.ReservationCancelledEvent;
import com.bookingnwt.notificationservice.events.ReservationCreatedEvent;
import com.bookingnwt.notificationservice.model.Notification;
import com.bookingnwt.notificationservice.repository.NotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAGA PATTERN — Notification Service Listener.
 *
 * Sluša payment evente i kreira notifikacije za korisnika:
 *   PAYMENT_COMPLETED → "Vaša rezervacija je potvrđena i plaćanje je uspješno obrađeno."
 *   PAYMENT_FAILED    → "Vaša rezervacija je otkazana jer plaćanje nije uspjelo."
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaNotificationListener {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.notification}")
    @Transactional
    public void handleMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "";

            switch (eventType) {
                case "PAYMENT_COMPLETED"  -> handlePaymentCompleted(message);
                case "PAYMENT_FAILED"     -> handlePaymentFailed(message);
                case "RESERVATION_CREATED" -> handleReservationCreated(message);
                case "RESERVATION_CANCELLED" -> handleReservationCancelled(message);
                default -> log.warn("Neprepoznat eventType na notification queue: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Greška pri obradi poruke na notification queue: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentCompleted(String message) throws Exception {
        PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
        log.info("Kreiranje notifikacije za uspješno plaćanje: reservationId={}, guestId={}",
                event.getReservationId(), event.getGuestId());

        Notification notification = new Notification(
                event.getGuestId(),
                "POTVRDA_REZERVACIJE",
                "Rezervacija potvrđena",
                String.format(
                        "Vaša rezervacija #%d je uspješno potvrđena. " +
                        "Plaćanje od %.2f %s je obrađeno. Ugodan boravak!",
                        event.getReservationId(),
                        event.getAmount(),
                        event.getCurrency()
                ),
                event.getReservationId()
        );

        notificationRepository.save(notification);
        log.info("Notifikacija POTVRDA_REZERVACIJE kreirana za korisnika {}", event.getGuestId());
    }

    private void handlePaymentFailed(String message) throws Exception {
        PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
        log.warn("Kreiranje notifikacije za neuspješno plaćanje: reservationId={}, guestId={}, razlog={}",
                event.getReservationId(), event.getGuestId(), event.getReason());

        Notification notification = new Notification(
                event.getGuestId(),
                "OTKAZANA_REZERVACIJA",
                "Rezervacija otkazana",
                String.format(
                        "Nažalost, vaša rezervacija #%d je otkazana jer plaćanje nije uspjelo. " +
                        "Razlog: %s. Molimo provjerite stanje vašeg novčanika.",
                        event.getReservationId(),
                        event.getReason()
                ),
                event.getReservationId()
        );

        notificationRepository.save(notification);
        log.warn("Notifikacija OTKAZANA_REZERVACIJA kreirana za korisnika {}", event.getGuestId());
    }

    /**
     * BUG 2 — Host dobija notifikaciju cim gost kreira rezervaciju za njegov smjestaj
     * (nezavisno od ishoda Saga payment-a).
     */
    private void handleReservationCreated(String message) throws Exception {
        ReservationCreatedEvent event = objectMapper.readValue(message, ReservationCreatedEvent.class);
        if (event.getHostId() == null) {
            log.warn("RESERVATION_CREATED event bez hostId — preskacem host notifikaciju (Reservation={})",
                    event.getReservationId());
            return;
        }
        log.info("Kreiranje host notifikacije za rezervaciju {}: hostId={}, guestId={}",
                event.getReservationId(), event.getHostId(), event.getUserId());

        Notification notification = new Notification(
                event.getHostId(),
                "NOVA_REZERVACIJA",
                "Nova rezervacija",
                String.format(
                        "Gost #%d je kreirao rezervaciju #%d za vaš smještaj #%d. " +
                        "Pratite status u Mojim smještajima.",
                        event.getUserId(),
                        event.getReservationId(),
                        event.getPropertyId()
                ),
                event.getReservationId()
        );
        notificationRepository.save(notification);
        log.info("Notifikacija NOVA_REZERVACIJA kreirana za hosta {}", event.getHostId());
    }

    /**
     * BUG A — Host dobija notifikaciju kad gost OTKAŽE rezervaciju za njegov smještaj.
     * Bez ovoga, host vidi kalendar promijenjen "ničim izazvano".
     */
    private void handleReservationCancelled(String message) throws Exception {
        ReservationCancelledEvent event = objectMapper.readValue(message, ReservationCancelledEvent.class);
        if (event.getHostId() == null) {
            log.warn("RESERVATION_CANCELLED event bez hostId — preskacem host notifikaciju (Reservation={})",
                    event.getReservationId());
            return;
        }
        log.info("Kreiranje host notifikacije za otkazanu rezervaciju {}: hostId={}",
                event.getReservationId(), event.getHostId());

        Notification notification = new Notification(
                event.getHostId(),
                "OTKAZANA_REZERVACIJA",
                "Otkazana rezervacija",
                String.format(
                        "Gost #%d je otkazao rezervaciju #%d za vaš smještaj #%d. " +
                        "Termin je oslobođen i ponovo dostupan za booking.",
                        event.getGuestId(),
                        event.getReservationId(),
                        event.getPropertyId()
                ),
                event.getReservationId()
        );
        notificationRepository.save(notification);
        log.info("Notifikacija OTKAZANA_REZERVACIJA kreirana za hosta {}", event.getHostId());
    }
}
