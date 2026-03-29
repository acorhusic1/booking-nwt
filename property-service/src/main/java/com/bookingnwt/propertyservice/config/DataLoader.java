package com.bookingnwt.propertyservice.config;

import com.bookingnwt.propertyservice.model.*;
import com.bookingnwt.propertyservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

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
                                        WishlistItemRepository wishlistItemRepo) {
        return args -> {
            // --- Amenities ---
            Amenity wifi = amenityRepo.save(new Amenity("WiFi", AmenityCategory.BASIC));
            Amenity parking = amenityRepo.save(new Amenity("Parking", AmenityCategory.BASIC));
            Amenity klima = amenityRepo.save(new Amenity("Klima uređaj", AmenityCategory.BASIC));
            Amenity tv = amenityRepo.save(new Amenity("TV", AmenityCategory.BASIC));
            Amenity kuhinja = amenityRepo.save(new Amenity("Kuhinja", AmenityCategory.BASIC));
            Amenity bazen = amenityRepo.save(new Amenity("Bazen", AmenityCategory.LUXURY));
            Amenity jacuzzi = amenityRepo.save(new Amenity("Jacuzzi", AmenityCategory.LUXURY));
            Amenity sauna = amenityRepo.save(new Amenity("Sauna", AmenityCategory.LUXURY));
            Amenity vatrogasniAparat = amenityRepo.save(new Amenity("Vatrogasni aparat", AmenityCategory.SAFETY));
            Amenity detektorDima = amenityRepo.save(new Amenity("Detektor dima", AmenityCategory.SAFETY));

            // --- Property 1: Apartman u Sarajevu ---
            Property p1 = new Property(2L, "Apartman Baščaršija",
                    "Prekrasan apartman u srcu Sarajeva, 5 minuta hoda od Baščaršije",
                    "Ferhadija 15", "Sarajevo", "Bosna i Hercegovina",
                    new BigDecimal("43.8563"), new BigDecimal("18.4131"), 4);
            propertyRepo.save(p1);
            p1.getAmenities().add(wifi);
            p1.getAmenities().add(klima);
            p1.getAmenities().add(tv);
            p1.getAmenities().add(detektorDima);
            propertyRepo.save(p1);

            imageRepo.save(new PropertyImage(p1, "/images/bascarsija-1.jpg", true));
            imageRepo.save(new PropertyImage(p1, "/images/bascarsija-2.jpg", false));

            pricingRepo.save(new PricingRule(p1, new BigDecimal("75.00"), new BigDecimal("90.00"),
                    2, 30, 10, 7));

            // --- Property 2: Vila u Mostaru ---
            Property p2 = new Property(2L, "Vila Stari Most",
                    "Luksuzna vila sa pogledom na Stari most i rijeku Neretvu",
                    "Rade Bitange 8", "Mostar", "Bosna i Hercegovina",
                    new BigDecimal("43.3372"), new BigDecimal("17.7928"), 8);
            propertyRepo.save(p2);
            p2.getAmenities().add(wifi);
            p2.getAmenities().add(parking);
            p2.getAmenities().add(klima);
            p2.getAmenities().add(bazen);
            p2.getAmenities().add(kuhinja);
            p2.getAmenities().add(vatrogasniAparat);
            propertyRepo.save(p2);

            imageRepo.save(new PropertyImage(p2, "/images/stari-most-1.jpg", true));
            imageRepo.save(new PropertyImage(p2, "/images/stari-most-2.jpg", false));
            imageRepo.save(new PropertyImage(p2, "/images/stari-most-3.jpg", false));

            pricingRepo.save(new PricingRule(p2, new BigDecimal("150.00"), new BigDecimal("180.00"),
                    3, 14, 15, 7));

            // --- Property 3: Hostel u Tuzli ---
            Property p3 = new Property(3L, "Hostel Pannonica",
                    "Moderno opremljen hostel u blizini Panonskih jezera",
                    "Turalibegova 50", "Tuzla", "Bosna i Hercegovina",
                    new BigDecimal("44.5384"), new BigDecimal("18.6763"), 20);
            propertyRepo.save(p3);
            p3.getAmenities().add(wifi);
            p3.getAmenities().add(tv);
            p3.getAmenities().add(detektorDima);
            propertyRepo.save(p3);

            imageRepo.save(new PropertyImage(p3, "/images/pannonica-1.jpg", true));

            pricingRepo.save(new PricingRule(p3, new BigDecimal("25.00"), new BigDecimal("30.00"),
                    1, 60, 20, 14));

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

            System.out.println("=== Property Service: Učitano " + propertyRepo.count() + " objekata ===");
            System.out.println("=== Property Service: Učitano " + amenityRepo.count() + " sadržaja ===");
            System.out.println("=== Property Service: Učitano " + imageRepo.count() + " slika ===");
            System.out.println("=== Property Service: Učitano " + pricingRepo.count() + " cjenovnika ===");
            System.out.println("=== Property Service: Učitano " + calendarRepo.count() + " blokova kalendara ===");
            System.out.println("=== Property Service: Učitano " + seasonalRepo.count() + " sezonskih pravila ===");
            System.out.println("=== Property Service: Učitano " + wishlistRepo.count() + " lista želja ===");
        };
    }
}
