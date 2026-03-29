package com.bookingnwt.systemevents.config;

import com.bookingnwt.systemevents.model.AuditLog;
import com.bookingnwt.systemevents.repository.AuditLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initAuditData(AuditLogRepository auditRepo) {
        return args -> {
            auditRepo.save(new AuditLog(2L, "CREATE", "PROPERTY", 1L,
                    "Kreiran objekat: Apartman Baščaršija, Sarajevo", "192.168.1.10"));

            auditRepo.save(new AuditLog(2L, "CREATE", "PROPERTY", 2L,
                    "Kreiran objekat: Vila Stari Most, Mostar", "192.168.1.10"));

            auditRepo.save(new AuditLog(3L, "CREATE", "PROPERTY", 3L,
                    "Kreiran objekat: Hostel Pannonica, Tuzla", "192.168.1.15"));

            auditRepo.save(new AuditLog(4L, "CREATE", "RESERVATION", 1L,
                    "Kreirana rezervacija za Apartman Baščaršija (10-15. maj 2026)", "192.168.1.20"));

            auditRepo.save(new AuditLog(2L, "UPDATE", "RESERVATION", 1L,
                    "Rezervacija potvrđena od strane domaćina", "192.168.1.10"));

            auditRepo.save(new AuditLog(5L, "CREATE", "RESERVATION", 2L,
                    "Kreirana rezervacija za Vila Stari Most (1-8. jul 2026)", "192.168.1.25"));

            auditRepo.save(new AuditLog(4L, "CREATE", "PAYMENT", 1L,
                    "Plaćanje 375.00 BAM za rezervaciju #1 putem WALLET", "192.168.1.20"));

            auditRepo.save(new AuditLog(6L, "CREATE", "REVIEW", 1L,
                    "Recenzija za Hostel Pannonica - ocjena 4.2", "192.168.1.30"));

            auditRepo.save(new AuditLog(1L, "UPDATE", "IDENTITY_VERIFICATION", 1L,
                    "Verifikacija domaćina Emir Duvnjak odobrena", "192.168.1.1"));

            auditRepo.save(new AuditLog(2L, "UPDATE", "PRICING_RULE", 1L,
                    "Ažurirana cijena za Apartman Baščaršija: bazna 75 BAM, vikend 90 BAM", "192.168.1.10"));

            System.out.println("=== System Events Service: Učitano " + auditRepo.count() + " audit logova ===");
        };
    }
}
