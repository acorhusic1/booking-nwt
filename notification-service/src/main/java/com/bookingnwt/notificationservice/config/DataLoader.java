package com.bookingnwt.notificationservice.config;

import com.bookingnwt.notificationservice.model.*;
import com.bookingnwt.notificationservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initNotificationData(NotificationRepository notifRepo,
                                            ConversationRepository convRepo,
                                            MessageRepository msgRepo) {
        return args -> {
            // Idempotent — preskoci ako podaci vec postoje (ddl-auto=update zadrzava)
            if (notifRepo.count() > 0) {
                System.out.println("=== Notification Service: DB vec ima podatke, preskacem seed ===");
                return;
            }

            // --- Notifikacije ---
            notifRepo.save(new Notification(2L, "NOVA_REZERVACIJA",
                    "Nova rezervacija", "Gost Benjamin je kreirao rezervaciju za Apartman Baščaršija (10-15. maj 2026)", 1L));

            notifRepo.save(new Notification(4L, "POTVRDA_REZERVACIJE",
                    "Rezervacija potvrđena", "Vaša rezervacija za Apartman Baščaršija je potvrđena!", 1L));

            notifRepo.save(new Notification(2L, "NOVA_REZERVACIJA",
                    "Nova rezervacija", "Gost Kenan je kreirao rezervaciju za Vila Stari Most (1-8. jul 2026)", 2L));

            Notification n4 = new Notification(6L, "ZAHTJEV_ZA_RECENZIJU",
                    "Ocijenite boravak", "Kako vam se svidio boravak u Hostel Pannonica? Ostavite recenziju!", 3L);
            n4.setIsRead(true);
            n4.setReadAt(LocalDateTime.now().minusDays(20));
            notifRepo.save(n4);

            notifRepo.save(new Notification(4L, "PODSJETNIK",
                    "Podsjetnik na dolazak", "Vaš check-in u Apartman Baščaršija je sutra! Adresa: Ferhadija 15, Sarajevo", 1L));

            notifRepo.save(new Notification(2L, "NOVA_PORUKA",
                    "Nova poruka od gosta", "Benjamin vam je poslao poruku u vezi rezervacije.", 1L));

            // --- Konverzacije i poruke ---
            Conversation conv1 = convRepo.save(new Conversation(4L, 2L, 1L, 1L));
            msgRepo.save(new Message(conv1, 4L, "Pozdrav! Imam pitanje o apartmanu - da li ima parking u blizini?"));
            msgRepo.save(new Message(conv1, 2L, "Pozdrav Benjamin! Nažalost, apartman nema vlastiti parking, ali postoji javni parking 200m dalje, cijena je 5 KM/dan."));
            msgRepo.save(new Message(conv1, 4L, "Odlično, hvala na informaciji! Vidimo se 10. maja."));

            Conversation conv2 = convRepo.save(new Conversation(5L, 2L, 2L, 2L));
            msgRepo.save(new Message(conv2, 5L, "Zdravo! Da li je moguć raniji check-in, oko 12h?"));
            msgRepo.save(new Message(conv2, 2L, "Zdravo Kenan! Da, raniji check-in je moguć od 12h bez dodatne naplate."));

            System.out.println("=== Notification Service: Učitano " + notifRepo.count() + " notifikacija ===");
            System.out.println("=== Notification Service: Učitano " + convRepo.count() + " konverzacija ===");
            System.out.println("=== Notification Service: Učitano " + msgRepo.count() + " poruka ===");
        };
    }
}
