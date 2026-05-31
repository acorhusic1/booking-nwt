package com.bookingnwt.reservationservice.scheduler;

import com.bookingnwt.reservationservice.events.ReservationReminderEvent;
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
import java.util.HashSet;
import java.util.Set;

/**
 * F9 — Scheduler za:
 *   - Podsjetnik na dolazak (dan prije check-in-a): RESERVATION_REMINDER event
 *   - Zahtjev za recenziju (dan poslije check-out-a):  REVIEW_REQUEST event
 *
 * Trackujemo poslane notifikacije u memoriji da ne spamujemo (jednom po
 * rezervaciji). U produkciji bi to bilo perzistirano u DB (npr. notification_log).
 *
 * Pokrece se svakih 5 minuta — dovoljno za demo, nije strogo precizno na dan.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationReminderScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationEventPublisher publisher;

    // Lokalni dedup — preimenuje u Set<Long> "sent for arrival" / "sent for review"
    private final Set<Long> arrivalSent = new HashSet<>();
    private final Set<Long> reviewSent = new HashSet<>();

    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000) // svakih 5 min, prvi run 1 min poslije start-a
    @Transactional(readOnly = true)
    public void dispatchReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        // 1) Dan prije dolaska: CONFIRMED rezervacije sa checkIn == tomorrow
        for (Reservation r : reservationRepository.findByStatus(ReservationStatus.CONFIRMED)) {
            if (r.getCheckIn() != null && r.getCheckIn().equals(tomorrow) && !arrivalSent.contains(r.getId())) {
                publisher.publishReservationReminder(new ReservationReminderEvent(
                        r.getId(), r.getGuestId(), r.getPropertyId(),
                        r.getCheckIn(), r.getCheckOut(),
                        LocalDateTime.now(), "RESERVATION_REMINDER"
                ));
                arrivalSent.add(r.getId());
            }
        }

        // 2) Dan poslije odlaska: COMPLETED rezervacije sa checkOut == yesterday
        for (Reservation r : reservationRepository.findByStatus(ReservationStatus.COMPLETED)) {
            if (r.getCheckOut() != null && r.getCheckOut().equals(yesterday) && !reviewSent.contains(r.getId())) {
                publisher.publishReservationReminder(new ReservationReminderEvent(
                        r.getId(), r.getGuestId(), r.getPropertyId(),
                        r.getCheckIn(), r.getCheckOut(),
                        LocalDateTime.now(), "REVIEW_REQUEST"
                ));
                reviewSent.add(r.getId());
            }
        }
    }
}
