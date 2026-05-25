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
}
