package com.bookingnwt.userservice.config;

import com.bookingnwt.userservice.model.*;
import com.bookingnwt.userservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initUserData(UserRepository userRepo,
                                   IdentityVerificationRepository verificationRepo,
                                   UserPreferenceRepository preferenceRepo,
                                   org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            // --- Kreiranje korisnika ---
            String commonPass = passwordEncoder.encode("password123");

            User admin = new User("admin@bookingnwt.com", commonPass,
                    "Admin", "Sistema", "+38761000001", UserRole.ADMIN);
            userRepo.save(admin);

            User host1 = new User("emir.d@email.com", commonPass,
                    "Emir", "Duvnjak", "+38761100001", UserRole.HOST);
            userRepo.save(host1);

            User host2 = new User("ahmed.c@email.com", commonPass,
                    "Ahmed", "Čorhusić", "+38761100002", UserRole.HOST);
            userRepo.save(host2);

            User guest1 = new User("benjamin.h@email.com", commonPass,
                    "Benjamin", "Hadžihasanović", "+38762200001", UserRole.GUEST);
            userRepo.save(guest1);

            User guest2 = new User("kenan.a@email.com", commonPass,
                    "Kenan", "Abadžić", "+38762200002", UserRole.GUEST);
            userRepo.save(guest2);

            User guest3 = new User("marija.m@email.com", commonPass,
                    "Marija", "Marković", "+38763300001", UserRole.GUEST);
            userRepo.save(guest3);

            // --- Verifikacija domaćina ---
            IdentityVerification v1 = new IdentityVerification(host1, "LIČNA_KARTA", "LK-123456789");
            v1.setStatus(VerificationStatus.APPROVED);
            v1.setVerifiedAt(LocalDateTime.now());
            v1.setVerifiedBy(admin.getId());
            verificationRepo.save(v1);

            IdentityVerification v2 = new IdentityVerification(host2, "PASOŠ", "PA-987654321");
            v2.setStatus(VerificationStatus.PENDING);
            verificationRepo.save(v2);

            // --- Korisničke preferencije ---
            UserPreference pref1 = new UserPreference(guest1, "bs", "APARTMAN",
                    new BigDecimal("50.00"), new BigDecimal("200.00"));
            preferenceRepo.save(pref1);

            UserPreference pref2 = new UserPreference(guest2, "en", "HOTEL",
                    new BigDecimal("80.00"), new BigDecimal("300.00"));
            preferenceRepo.save(pref2);

            UserPreference pref3 = new UserPreference(guest3, "bs", "KUĆA_ZA_ODMOR",
                    new BigDecimal("100.00"), new BigDecimal("500.00"));
            preferenceRepo.save(pref3);

            System.out.println("=== User Service: Učitano " + userRepo.count() + " korisnika ===");
            System.out.println("=== User Service: Učitano " + verificationRepo.count() + " verifikacija ===");
            System.out.println("=== User Service: Učitano " + preferenceRepo.count() + " preferencija ===");
        };
    }
}
