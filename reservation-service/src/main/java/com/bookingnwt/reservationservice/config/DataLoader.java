package com.bookingnwt.reservationservice.config;

import com.bookingnwt.reservationservice.model.*;
import com.bookingnwt.reservationservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initReservationData(ReservationRepository reservationRepo,
                                           CancellationPolicyRepository policyRepo,
                                           PromoCodeRepository promoRepo,
                                           ProblemReportRepository problemRepo) {
        return args -> {
            // Idempotent — preskoci ako podaci vec postoje (ddl-auto=update zadrzava)
            if (reservationRepo.count() > 0 || policyRepo.count() > 0) {
                System.out.println("=== Reservation Service: DB vec ima podatke, preskacem seed ===");
                return;
            }

            // --- Politike otkazivanja ---
            CancellationPolicy flexible = policyRepo.save(
                    new CancellationPolicy(1L, "Fleksibilna", 7, 100, false));
            CancellationPolicy moderate = policyRepo.save(
                    new CancellationPolicy(2L, "Umjerena", 3, 50, false));
            CancellationPolicy strict = policyRepo.save(
                    new CancellationPolicy(3L, "Stroga", 0, 0, true));

            // --- Promotivni kodovi ---
            PromoCode promo1 = promoRepo.save(new PromoCode("LJETO2026", "Ljetni popust 15%",
                    DiscountType.PERCENTAGE, new BigDecimal("15.00"), 3,
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), 100, 1L));

            PromoCode promo2 = promoRepo.save(new PromoCode("WELCOME10", "Popust dobrodošlice 10 KM",
                    DiscountType.FIXED, new BigDecimal("10.00"), 1,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 500, 1L));

            promoRepo.save(new PromoCode("NWT2026", "NWT studentski popust 20%",
                    DiscountType.PERCENTAGE, new BigDecimal("20.00"), 2,
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 50, 1L));

            // --- Rezervacije ---
            Reservation r1 = reservationRepo.save(new Reservation(
                    4L, 2L, 1L,
                    LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 15),
                    2, new BigDecimal("375.00"), flexible, null));
            r1.setStatus(ReservationStatus.CONFIRMED);
            reservationRepo.save(r1);

            Reservation r2 = reservationRepo.save(new Reservation(
                    5L, 2L, 2L,
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 8),
                    4, new BigDecimal("1050.00"), moderate, promo1));
            r2.setStatus(ReservationStatus.CONFIRMED);
            reservationRepo.save(r2);

            Reservation r3 = reservationRepo.save(new Reservation(
                    6L, 3L, 3L,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5),
                    2, new BigDecimal("100.00"), flexible, promo2));
            r3.setStatus(ReservationStatus.COMPLETED);
            reservationRepo.save(r3);

            reservationRepo.save(new Reservation(
                    4L, 3L, 3L,
                    LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 18),
                    1, new BigDecimal("75.00"), strict, null));

            // --- Dodatne rezervacije rasprostranjene kroz cijelu godinu (za analytics) ---
            // Property 1 (host 2)
            saveCompleted(reservationRepo, 4L, 2L, 1L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 14), 2, "300.00", flexible);
            saveCompleted(reservationRepo, 5L, 2L, 1L, LocalDate.of(2026, 2, 5), LocalDate.of(2026, 2, 8), 3, "225.00", flexible);
            saveCompleted(reservationRepo, 6L, 2L, 1L, LocalDate.of(2026, 3, 12), LocalDate.of(2026, 3, 16), 2, "300.00", flexible);
            saveCompleted(reservationRepo, 4L, 2L, 1L, LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 24), 4, "300.00", flexible);
            saveActive(reservationRepo, 5L, 2L, 1L, LocalDate.of(2026, 5, 28), LocalDate.of(2026, 6, 2), 2, "375.00", flexible);
            // Property 2 (host 2) — luxury
            saveCompleted(reservationRepo, 4L, 2L, 2L, LocalDate.of(2026, 2, 14), LocalDate.of(2026, 2, 18), 6, "600.00", moderate);
            saveCompleted(reservationRepo, 6L, 2L, 2L, LocalDate.of(2026, 4, 18), LocalDate.of(2026, 4, 25), 8, "1050.00", moderate);
            saveConfirmed(reservationRepo, 5L, 2L, 2L, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 22), 5, "1260.00", moderate);
            saveConfirmed(reservationRepo, 4L, 2L, 2L, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 17), 6, "1260.00", moderate);
            // Property 3 (host 3) — hostel popularan
            saveCompleted(reservationRepo, 4L, 3L, 3L, LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 23), 4, "75.00", flexible);
            saveCompleted(reservationRepo, 5L, 3L, 3L, LocalDate.of(2026, 2, 25), LocalDate.of(2026, 2, 28), 6, "75.00", flexible);
            saveCompleted(reservationRepo, 6L, 3L, 3L, LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 10), 3, "125.00", flexible);
            saveCompleted(reservationRepo, 4L, 3L, 3L, LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 17), 5, "75.00", flexible);
            saveConfirmed(reservationRepo, 5L, 3L, 3L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7), 10, "150.00", flexible);
            // Property 4 (host 2)
            saveCompleted(reservationRepo, 6L, 2L, 4L, LocalDate.of(2026, 3, 18), LocalDate.of(2026, 3, 22), 2, "240.00", flexible);
            saveConfirmed(reservationRepo, 4L, 2L, 4L, LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10), 3, "300.00", flexible);
            // Property 5 (host 3) — Jahorina, zima
            saveCompleted(reservationRepo, 5L, 3L, 5L, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 12), 4, "840.00", moderate);
            saveCompleted(reservationRepo, 6L, 3L, 5L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 8), 6, "840.00", moderate);
            saveConfirmed(reservationRepo, 4L, 3L, 5L, LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 28), 5, "1200.00", moderate);
            // Property 6 (host 2) — Split ljeto
            saveConfirmed(reservationRepo, 5L, 2L, 6L, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 27), 4, "770.00", moderate);
            saveConfirmed(reservationRepo, 6L, 2L, 6L, LocalDate.of(2026, 7, 25), LocalDate.of(2026, 8, 1), 3, "980.00", moderate);
            // Property 7 (host 3) — Dubrovnik luxury
            saveCompleted(reservationRepo, 4L, 3L, 7L, LocalDate.of(2026, 4, 28), LocalDate.of(2026, 5, 4), 8, "1320.00", strict);
            saveConfirmed(reservationRepo, 5L, 3L, 7L, LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 12), 10, "1960.00", strict);
            // Property 8 (host 2) — Beograd jeftin
            saveCompleted(reservationRepo, 6L, 2L, 8L, LocalDate.of(2026, 1, 25), LocalDate.of(2026, 1, 30), 2, "225.00", flexible);
            saveCompleted(reservationRepo, 4L, 2L, 8L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), 2, "180.00", flexible);
            saveConfirmed(reservationRepo, 5L, 2L, 8L, LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 16), 2, "180.00", flexible);
            // Property 9 (host 3) — Kotor boutique
            saveCompleted(reservationRepo, 5L, 3L, 9L, LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 14), 4, "1080.00", moderate);
            saveConfirmed(reservationRepo, 4L, 3L, 9L, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 17), 6, "1540.00", moderate);
            // Property 10 (host 2) — Una river
            saveCompleted(reservationRepo, 6L, 2L, 10L, LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 9), 5, "380.00", flexible);
            saveConfirmed(reservationRepo, 4L, 2L, 10L, LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 24), 7, "720.00", flexible);

            // --- Prijave problema ---
            ProblemReport pr1 = new ProblemReport(r3, 6L, "ČISTOĆA",
                    "Kupaonica je bila nedovoljno čista pri dolasku.");
            pr1.setStatus(ProblemReportStatus.RESOLVED);
            problemRepo.save(pr1);

            ProblemReport pr2 = new ProblemReport(r1, 4L, "OPREMA",
                    "Klima uređaj ne radi ispravno.");
            problemRepo.save(pr2);

            System.out.println("=== Reservation Service: Učitano " + reservationRepo.count() + " rezervacija ===");
            System.out.println("=== Reservation Service: Učitano " + policyRepo.count() + " politika otkazivanja ===");
            System.out.println("=== Reservation Service: Učitano " + promoRepo.count() + " promo kodova ===");
            System.out.println("=== Reservation Service: Učitano " + problemRepo.count() + " prijava problema ===");
        };
    }

    // Helperi za seed — kreiraju rezervaciju i postave status u jednom pozivu
    private void saveCompleted(com.bookingnwt.reservationservice.repository.ReservationRepository repo,
                                long guestId, long hostId, long propertyId,
                                LocalDate ci, LocalDate co, int guests, String price,
                                com.bookingnwt.reservationservice.model.CancellationPolicy policy) {
        Reservation r = new Reservation(guestId, hostId, propertyId, ci, co, guests,
                new BigDecimal(price), policy, null);
        r.setStatus(ReservationStatus.COMPLETED);
        repo.save(r);
    }
    private void saveConfirmed(com.bookingnwt.reservationservice.repository.ReservationRepository repo,
                                long guestId, long hostId, long propertyId,
                                LocalDate ci, LocalDate co, int guests, String price,
                                com.bookingnwt.reservationservice.model.CancellationPolicy policy) {
        Reservation r = new Reservation(guestId, hostId, propertyId, ci, co, guests,
                new BigDecimal(price), policy, null);
        r.setStatus(ReservationStatus.CONFIRMED);
        repo.save(r);
    }
    private void saveActive(com.bookingnwt.reservationservice.repository.ReservationRepository repo,
                             long guestId, long hostId, long propertyId,
                             LocalDate ci, LocalDate co, int guests, String price,
                             com.bookingnwt.reservationservice.model.CancellationPolicy policy) {
        Reservation r = new Reservation(guestId, hostId, propertyId, ci, co, guests,
                new BigDecimal(price), policy, null);
        r.setStatus(ReservationStatus.ACTIVE);
        repo.save(r);
    }
}
