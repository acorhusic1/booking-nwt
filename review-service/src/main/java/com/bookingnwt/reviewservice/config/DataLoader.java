package com.bookingnwt.reviewservice.config;

import com.bookingnwt.reviewservice.model.Review;
import com.bookingnwt.reviewservice.repository.ReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initReviewData(ReviewRepository reviewRepo) {
        return args -> {
            // Recenzija za završenu rezervaciju #3 (gost 6, property 3, host 3)
            Review r1 = new Review(3L, 6L, 3L, 3L,
                    new BigDecimal("3.5"), new BigDecimal("4.5"),
                    new BigDecimal("5.0"), new BigDecimal("4.0"),
                    new BigDecimal("4.0"),
                    "Hostel je dobar za cijenu. Lokacija odlična, blizu jezera. " +
                    "Čistoća mogla biti bolja pri dolasku, ali osoblje je brzo reagovalo.");
            r1.setHostReply("Hvala na recenziji! Izvinjavamo se za čistoću, poduzeli smo mjere.");
            r1.setRepliedAt(LocalDateTime.now().minusDays(20));
            reviewRepo.save(r1);

            // Recenzija za property 1 (simulira prethodnu završenu rezervaciju)
            Review r2 = new Review(10L, 4L, 1L, 2L,
                    new BigDecimal("5.0"), new BigDecimal("5.0"),
                    new BigDecimal("4.5"), new BigDecimal("4.5"),
                    new BigDecimal("5.0"),
                    "Fantastičan apartman u centru Sarajeva! Sve je bilo savršeno, " +
                    "domaćin veoma ljubazan i komunikativan. Toplo preporučujem!");
            reviewRepo.save(r2);

            // Recenzija za property 2
            Review r3 = new Review(11L, 5L, 2L, 2L,
                    new BigDecimal("4.5"), new BigDecimal("5.0"),
                    new BigDecimal("4.0"), new BigDecimal("3.5"),
                    new BigDecimal("4.5"),
                    "Vila je prelijepa sa pogledom na Stari most. Cijena malo viša ali " +
                    "lokacija nadoknađuje. Bazen je bio odličan!");
            r3.setHostReply("Drago nam je da ste uživali! Dobrodošli ponovo.");
            r3.setRepliedAt(LocalDateTime.now().minusDays(5));
            reviewRepo.save(r3);

            System.out.println("=== Review Service: Učitano " + reviewRepo.count() + " recenzija ===");
        };
    }
}
