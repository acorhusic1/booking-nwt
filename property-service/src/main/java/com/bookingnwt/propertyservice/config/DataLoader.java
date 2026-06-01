package com.bookingnwt.propertyservice.config;

import com.bookingnwt.propertyservice.model.*;
import com.bookingnwt.propertyservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initPropertyData(PropertyRepository propertyRepo,
                                        AmenityRepository amenityRepo,
                                        PropertyImageRepository imageRepo,
                                        PricingRuleRepository pricingRepo,
                                        CalendarBlockRepository calendarRepo,
                                        SeasonalRuleRepository seasonalRepo,
                                        WishlistRepository wishlistRepo,
                                        WishlistItemRepository wishlistItemRepo,
                                        ReviewRepository reviewRepo) {
        return args -> {
            // Check if data already exists
            if (amenityRepo.count() > 0) {
                System.out.println("=== Property Service: Data already loaded, skipping initialization ===");
                return;
            }

            // --- Amenities ---
            Amenity wifi = amenityRepo.save(new Amenity("WiFi", AmenityCategory.BASIC));
            Amenity parking = amenityRepo.save(new Amenity("Parking", AmenityCategory.BASIC));
            Amenity klima = amenityRepo.save(new Amenity("Klima uređaj", AmenityCategory.BASIC));
            Amenity tv = amenityRepo.save(new Amenity("TV", AmenityCategory.BASIC));
            Amenity kuhinja = amenityRepo.save(new Amenity("Kuhinja", AmenityCategory.BASIC));
            Amenity bazen = amenityRepo.save(new Amenity("Bazen", AmenityCategory.LUXURY));
            amenityRepo.save(new Amenity("Jacuzzi", AmenityCategory.LUXURY));
            amenityRepo.save(new Amenity("Sauna", AmenityCategory.LUXURY));
            Amenity vatrogasniAparat = amenityRepo.save(new Amenity("Vatrogasni aparat", AmenityCategory.SAFETY));
            Amenity detektorDima = amenityRepo.save(new Amenity("Detektor dima", AmenityCategory.SAFETY));

            // Helper za moderation APPROVED + slike. Sve seed properties su odmah javno
            // vidljive (admin moderacija je za one koje korisnik kreira kroz UI).
            // Slike koristimo Unsplash random — svaki property ima poseban seed.

            // --- Property 1 ---
            Property p1 = new Property(2L, "Apartman Baščaršija",
                    "Prekrasan apartman u srcu Sarajeva, 5 minuta hoda od Baščaršije. " +
                    "Moderno opremljen, sa prelijepim pogledom na grad. Idealno za kratki vikend boravak.",
                    "Ferhadija 15", "Sarajevo", "Bosna i Hercegovina",
                    new BigDecimal("43.8563"), new BigDecimal("18.4131"), 4);
            p1.setModerationStatus("APPROVED");
            propertyRepo.save(p1);
            p1.getAmenities().add(wifi); p1.getAmenities().add(klima);
            p1.getAmenities().add(tv); p1.getAmenities().add(detektorDima);
            propertyRepo.save(p1);
            imageRepo.save(new PropertyImage(p1, "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800", true));
            imageRepo.save(new PropertyImage(p1, "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800", false));
            pricingRepo.save(new PricingRule(p1, new BigDecimal("75.00"), new BigDecimal("90.00"), 2, 30, 10, 7));

            // --- Property 2 ---
            Property p2 = new Property(2L, "Vila Stari Most",
                    "Luksuzna vila sa pogledom na Stari most i rijeku Neretvu. Bazen, vrt, parking. " +
                    "Idealna za porodična okupljanja i odmor u mirnom dijelu Mostara.",
                    "Rade Bitange 8", "Mostar", "Bosna i Hercegovina",
                    new BigDecimal("43.3372"), new BigDecimal("17.7928"), 8);
            p2.setModerationStatus("APPROVED");
            propertyRepo.save(p2);
            p2.getAmenities().add(wifi); p2.getAmenities().add(parking); p2.getAmenities().add(klima);
            p2.getAmenities().add(bazen); p2.getAmenities().add(kuhinja); p2.getAmenities().add(vatrogasniAparat);
            propertyRepo.save(p2);
            imageRepo.save(new PropertyImage(p2, "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800", true));
            imageRepo.save(new PropertyImage(p2, "https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800", false));
            pricingRepo.save(new PricingRule(p2, new BigDecimal("150.00"), new BigDecimal("180.00"), 3, 14, 15, 7));

            // --- Property 3 ---
            Property p3 = new Property(3L, "Hostel Pannonica",
                    "Moderno opremljen hostel u blizini Panonskih jezera. Idealno za grupna putovanja " +
                    "i mlade goste. Zajednička kuhinja, dnevni boravak, terasa za druženje.",
                    "Turalibegova 50", "Tuzla", "Bosna i Hercegovina",
                    new BigDecimal("44.5384"), new BigDecimal("18.6763"), 20);
            p3.setModerationStatus("APPROVED");
            propertyRepo.save(p3);
            p3.getAmenities().add(wifi); p3.getAmenities().add(tv); p3.getAmenities().add(detektorDima);
            propertyRepo.save(p3);
            imageRepo.save(new PropertyImage(p3, "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800", true));
            pricingRepo.save(new PricingRule(p3, new BigDecimal("25.00"), new BigDecimal("30.00"), 1, 60, 20, 14));

            // --- Property 4: Apartman u Banja Luci ---
            Property p4 = new Property(2L, "Apartman Kastel",
                    "Centralna lokacija pored Kastel tvrđave. Pješačka udaljenost od svih atrakcija, " +
                    "restorana i shopping centara. Brand new namještaj.",
                    "Bana Lazarevića 12", "Banja Luka", "Bosna i Hercegovina",
                    new BigDecimal("44.7722"), new BigDecimal("17.1910"), 3);
            p4.setModerationStatus("APPROVED");
            propertyRepo.save(p4);
            p4.getAmenities().add(wifi); p4.getAmenities().add(klima); p4.getAmenities().add(tv);
            p4.getAmenities().add(kuhinja); p4.getAmenities().add(parking);
            propertyRepo.save(p4);
            imageRepo.save(new PropertyImage(p4, "https://images.unsplash.com/photo-1522156373667-4c7234bbd804?w=800", true));
            pricingRepo.save(new PricingRule(p4, new BigDecimal("60.00"), new BigDecimal("75.00"), 1, 21, 12, 7));

            // --- Property 5: Vikendica Jahorina ---
            Property p5 = new Property(3L, "Vikendica Jahorina",
                    "Brvnara na padinama Jahorine, 200m od skijaškog lifta. Topao kamin, sauna, " +
                    "veliki dnevni boravak. Idealno za zimske odmore.",
                    "Trnovo BB", "Pale", "Bosna i Hercegovina",
                    new BigDecimal("43.7333"), new BigDecimal("18.5667"), 6);
            p5.setModerationStatus("APPROVED");
            propertyRepo.save(p5);
            p5.getAmenities().add(wifi); p5.getAmenities().add(parking); p5.getAmenities().add(kuhinja);
            p5.getAmenities().add(tv); p5.getAmenities().add(vatrogasniAparat);
            propertyRepo.save(p5);
            imageRepo.save(new PropertyImage(p5, "https://images.unsplash.com/photo-1542718610-a1d656d1884c?w=800", true));
            pricingRepo.save(new PricingRule(p5, new BigDecimal("120.00"), new BigDecimal("150.00"), 2, 14, 10, 7));

            // --- Property 6: Apartman u Splitu (Hrvatska) ---
            Property p6 = new Property(2L, "Apartman Bačvice Beach",
                    "Samo 100m od plaže Bačvice. Klimatizovan, opremljena kuhinja, balkon sa pogledom na more.",
                    "Šetalište bačvice 5", "Split", "Hrvatska",
                    new BigDecimal("43.5081"), new BigDecimal("16.4402"), 4);
            p6.setModerationStatus("APPROVED");
            propertyRepo.save(p6);
            p6.getAmenities().add(wifi); p6.getAmenities().add(klima); p6.getAmenities().add(kuhinja);
            p6.getAmenities().add(tv);
            propertyRepo.save(p6);
            imageRepo.save(new PropertyImage(p6, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800", true));
            pricingRepo.save(new PricingRule(p6, new BigDecimal("110.00"), new BigDecimal("140.00"), 3, 14, 15, 7));

            // --- Property 7: Vila Dubrovnik ---
            Property p7 = new Property(3L, "Villa Dubrovnik Old Town",
                    "Vila u srcu Starog grada, kameni zidovi, terasa sa pogledom na luku. " +
                    "Privatni parking u garaži, doručak u cijeni.",
                    "Ulica od Puča 14", "Dubrovnik", "Hrvatska",
                    new BigDecimal("42.6507"), new BigDecimal("18.0944"), 10);
            p7.setModerationStatus("APPROVED");
            propertyRepo.save(p7);
            p7.getAmenities().add(wifi); p7.getAmenities().add(klima); p7.getAmenities().add(bazen);
            p7.getAmenities().add(parking); p7.getAmenities().add(kuhinja); p7.getAmenities().add(vatrogasniAparat);
            propertyRepo.save(p7);
            imageRepo.save(new PropertyImage(p7, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800", true));
            pricingRepo.save(new PricingRule(p7, new BigDecimal("220.00"), new BigDecimal("280.00"), 4, 21, 12, 7));

            // --- Property 8: Studio Beograd ---
            Property p8 = new Property(2L, "Studio Knez Mihailova",
                    "Mali ali pametan studio u pješačkoj zoni Knez Mihailove. Sve potrebno za 2 osobe.",
                    "Knez Mihailova 22", "Beograd", "Srbija",
                    new BigDecimal("44.8176"), new BigDecimal("20.4569"), 2);
            p8.setModerationStatus("APPROVED");
            propertyRepo.save(p8);
            p8.getAmenities().add(wifi); p8.getAmenities().add(klima); p8.getAmenities().add(tv);
            propertyRepo.save(p8);
            imageRepo.save(new PropertyImage(p8, "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800", true));
            pricingRepo.save(new PricingRule(p8, new BigDecimal("45.00"), new BigDecimal("55.00"), 1, 30, 10, 14));

            // --- Property 9: Hotel Kotor ---
            Property p9 = new Property(3L, "Boutique Hotel Kotor",
                    "Boutique hotel u Starom gradu Kotora, UNESCO baština. Restoran, room service, spa.",
                    "Stari Grad 100", "Kotor", "Crna Gora",
                    new BigDecimal("42.4247"), new BigDecimal("18.7712"), 30);
            p9.setModerationStatus("APPROVED");
            propertyRepo.save(p9);
            p9.getAmenities().add(wifi); p9.getAmenities().add(klima); p9.getAmenities().add(tv);
            p9.getAmenities().add(parking); p9.getAmenities().add(bazen); p9.getAmenities().add(detektorDima);
            propertyRepo.save(p9);
            imageRepo.save(new PropertyImage(p9, "https://images.unsplash.com/photo-1455587734955-081b22074882?w=800", true));
            pricingRepo.save(new PricingRule(p9, new BigDecimal("180.00"), new BigDecimal("220.00"), 2, 14, 10, 7));

            // --- Property 10: Vila Bihać ---
            Property p10 = new Property(2L, "Vila Una River",
                    "Vila uz rijeku Unu, idealna za ribolovce i prirodne avanture. Kajak, čamac, roštilj.",
                    "Klokot bb", "Bihać", "Bosna i Hercegovina",
                    new BigDecimal("44.8167"), new BigDecimal("15.8709"), 8);
            p10.setModerationStatus("APPROVED");
            propertyRepo.save(p10);
            p10.getAmenities().add(wifi); p10.getAmenities().add(parking); p10.getAmenities().add(kuhinja);
            p10.getAmenities().add(tv);
            propertyRepo.save(p10);
            imageRepo.save(new PropertyImage(p10, "https://images.unsplash.com/photo-1502209524164-acea936639a2?w=800", true));
            pricingRepo.save(new PricingRule(p10, new BigDecimal("95.00"), new BigDecimal("120.00"), 2, 14, 12, 7));

            // --- Calendar Blocks ---
            calendarRepo.save(new CalendarBlock(p1,
                    LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 20),
                    "Renoviranje kupaonice", 2L));
            calendarRepo.save(new CalendarBlock(p2,
                    LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 31),
                    "Privatna upotreba - praznici", 2L));

            // --- Seasonal Rules ---
            seasonalRepo.save(new SeasonalRule(p1, "Ljeto 2026",
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), 30, 3));
            seasonalRepo.save(new SeasonalRule(p2, "Ljeto 2026",
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), 50, 5));
            seasonalRepo.save(new SeasonalRule(p1, "Nova Godina",
                    LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 3), 40, 3));

            // --- Wishlists ---
            Wishlist wl1 = wishlistRepo.save(new Wishlist(4L, "Ljeto 2026"));
            wishlistItemRepo.save(new WishlistItem(wl1, p1.getId()));
            wishlistItemRepo.save(new WishlistItem(wl1, p2.getId()));

            Wishlist wl2 = wishlistRepo.save(new Wishlist(5L, "Vikend getaway"));
            wishlistItemRepo.save(new WishlistItem(wl2, p2.getId()));

            // --- Reviews ---
            Review r1 = new Review(3L, 6L, p3.getId(), 3L,
                    new BigDecimal("4.5"), new BigDecimal("4.0"),
                    new BigDecimal("5.0"), new BigDecimal("4.5"),
                    new BigDecimal("4.0"), "Odličan hostel, čist i udoban. Osoblje vrlo ljubazno.");
            r1.setHostReply("Hvala vam na lijepim riječima! Nadamo se ponovnom dolasku.");
            r1.setRepliedAt(LocalDateTime.now().minusDays(25));
            reviewRepo.save(r1);

            Review r2 = new Review(10L, 4L, p1.getId(), 2L,
                    new BigDecimal("5.0"), new BigDecimal("5.0"),
                    new BigDecimal("4.5"), new BigDecimal("4.0"),
                    new BigDecimal("5.0"), "Fantastična lokacija, apartman je baš kao na slikama.");
            reviewRepo.save(r2);

            Review r3 = new Review(11L, 5L, p2.getId(), 2L,
                    new BigDecimal("5.0"), new BigDecimal("5.0"),
                    new BigDecimal("5.0"), new BigDecimal("4.5"),
                    new BigDecimal("5.0"), "Vila je predivna, pogled na Stari most je nezaboravan!");
            r3.setHostReply("Drago nam je da ste uživali! Dobrodošli ponovo.");
            r3.setRepliedAt(LocalDateTime.now().minusDays(10));
            reviewRepo.save(r3);

            // Više recenzija da analitika i prosjeci budu realniji
            reviewRepo.save(seedReview(20L, 4L, p1.getId(), 2L,
                    "5.0", "4.5", "4.5", "5.0", "5.0",
                    "Centralna lokacija, sve u pješačkoj zoni. Domaćin vrlo komunikativan."));
            reviewRepo.save(seedReview(21L, 6L, p1.getId(), 2L,
                    "4.0", "5.0", "4.0", "4.5", "4.0",
                    "Apartman je manji nego što izgleda na slikama, ali sve potrebno za par."));
            reviewRepo.save(seedReview(22L, 5L, p2.getId(), 2L,
                    "5.0", "5.0", "4.5", "4.0", "5.0",
                    "Bazen je čist, vila luksuzna. Vrijedi svake KM."));
            reviewRepo.save(seedReview(23L, 4L, p3.getId(), 3L,
                    "4.0", "4.5", "4.5", "5.0", "4.0",
                    "Najjeftinija opcija u Tuzli sa svim sadržajima. Solid."));
            reviewRepo.save(seedReview(24L, 6L, p3.getId(), 3L,
                    "3.5", "4.0", "5.0", "5.0", "4.5",
                    "Hostel je bučan navečer ali dobro za grupna druženja."));
            reviewRepo.save(seedReview(25L, 5L, p4.getId(), 2L,
                    "4.5", "5.0", "4.0", "4.0", "4.5",
                    "Banja Luka centar, sve dostupno pješke. Dobar deal."));
            reviewRepo.save(seedReview(26L, 4L, p5.getId(), 3L,
                    "5.0", "5.0", "5.0", "5.0", "4.5",
                    "Idealno za skijanje na Jahorini. Sauna je vrhunska!"));
            reviewRepo.save(seedReview(27L, 6L, p5.getId(), 3L,
                    "5.0", "5.0", "4.5", "4.5", "5.0",
                    "Brvnara je kao iz filma. Pogled prema padinama je magičan."));
            reviewRepo.save(seedReview(28L, 5L, p6.getId(), 2L,
                    "4.5", "5.0", "4.0", "4.5", "4.5",
                    "Split, 100m od plaže — tačno kako piše. Apartman opremljen sa sve."));
            reviewRepo.save(seedReview(29L, 4L, p7.getId(), 3L,
                    "5.0", "5.0", "5.0", "4.0", "5.0",
                    "Dubrovnik Stari Grad — vila je iznad očekivanja. Skupo ali vrijedi."));
            reviewRepo.save(seedReview(30L, 6L, p7.getId(), 3L,
                    "5.0", "5.0", "4.5", "4.5", "5.0",
                    "Pogled na luku, kameni zidovi, terasa za uveče — savršeno."));
            reviewRepo.save(seedReview(31L, 5L, p8.getId(), 2L,
                    "4.0", "4.5", "4.0", "5.0", "4.5",
                    "Mali studio ali sve što treba za 2 osobe. Centar Beograda."));
            reviewRepo.save(seedReview(32L, 4L, p9.getId(), 3L,
                    "5.0", "5.0", "5.0", "5.0", "5.0",
                    "Kotor je čudo prirode, hotel još veće. Spa je top!"));
            reviewRepo.save(seedReview(33L, 6L, p10.getId(), 2L,
                    "5.0", "4.5", "4.0", "5.0", "4.5",
                    "Una je rijeka snova, vila sa savršenim pristupom. Domaćin pomaže sa kajakom."));

            System.out.println("=== Property Service: Učitano " + propertyRepo.count() + " objekata ===");
            System.out.println("=== Property Service: Učitano " + amenityRepo.count() + " sadržaja ===");
            System.out.println("=== Property Service: Učitano " + imageRepo.count() + " slika ===");
            System.out.println("=== Property Service: Učitano " + pricingRepo.count() + " cjenovnika ===");
            System.out.println("=== Property Service: Učitano " + calendarRepo.count() + " blokova kalendara ===");
            System.out.println("=== Property Service: Učitano " + seasonalRepo.count() + " sezonskih pravila ===");
            System.out.println("=== Property Service: Učitano " + wishlistRepo.count() + " lista želja ===");
            System.out.println("=== Property Service: Učitano " + reviewRepo.count() + " recenzija ===");
        };
    }

    // Helper za kreiranje review-a bez dugog konstruktora ponavljanja
    private Review seedReview(Long reservationId, Long guestId, Long propertyId, Long hostId,
                              String clean, String location, String comm, String value, String accuracy,
                              String comment) {
        return new Review(reservationId, guestId, propertyId, hostId,
                new BigDecimal(clean), new BigDecimal(location), new BigDecimal(comm),
                new BigDecimal(value), new BigDecimal(accuracy), comment);
    }
}
