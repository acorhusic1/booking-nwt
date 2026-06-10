package com.bookingnwt.reservationservice.scheduler;

import com.bookingnwt.reservationservice.events.ReservationCompletedEvent;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.publisher.ReservationEventPublisher;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Automatska tranzicija statusa rezervacija na osnovu datuma.
 *
 *   CONFIRMED + checkIn <= today  → ACTIVE    (gost je trenutno u smjestaju)
 *   ACTIVE/CONFIRMED + checkOut < today  → COMPLETED  (boravak je zavrsen)
 *
 * Pokrece se svake minute zbog brze demo iteracije. U produkciji bi obicno
 * bilo dnevno (npr. 02:00).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationStatusScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000) // svake minute, prvi run 30s nakon start-a
    @Transactional
    public void autoTransitionStatuses() {
        LocalDate today = LocalDate.now();
        int activated = 0;
        int completed = 0;

        // CONFIRMED + check-in dosao → ACTIVE
        List<Reservation> toActivate = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        for (Reservation r : toActivate) {
            if (r.getCheckIn() != null && !r.getCheckIn().isAfter(today)
                    && r.getCheckOut() != null && r.getCheckOut().isAfter(today)) {
                r.setStatus(ReservationStatus.ACTIVE);
                reservationRepository.save(r);
                activated++;
            }
        }

        // ACTIVE ili CONFIRMED + check-out prosao → COMPLETED
        List<Reservation> activeOrConfirmed = reservationRepository.findByStatus(ReservationStatus.ACTIVE);
        activeOrConfirmed.addAll(reservationRepository.findByStatus(ReservationStatus.CONFIRMED));
        for (Reservation r : activeOrConfirmed) {
            if (r.getCheckOut() != null && r.getCheckOut().isBefore(today)) {
                r.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(r);
                completed++;

                // F19 — tek SADA payment-service isplacuje hosta ("Domacin prima
                // isplatu nakon uspjesno zavrsenog boravka gosta"). Listener u
                // payment-service-u je idempotentan pa eventualni dupli event
                // (restart schedulera) ne pravi duplu isplatu.
                try {
                    eventPublisher.publishReservationCompleted(new ReservationCompletedEvent(
                            r.getId(), r.getPropertyId(), r.getGuestId(), r.getHostId(),
                            r.getTotalPrice(), "BAM", LocalDateTime.now(), "RESERVATION_COMPLETED"));
                } catch (Exception e) {
                    log.warn("⚠️ ReservationCompletedEvent publish nije uspio za rezervaciju {}: {}",
                            r.getId(), e.getMessage());
                }
            }
        }

        if (activated + completed > 0) {
            log.info("📅 Auto-tranzicija: {} → ACTIVE, {} → COMPLETED", activated, completed);
        }
    }
}
