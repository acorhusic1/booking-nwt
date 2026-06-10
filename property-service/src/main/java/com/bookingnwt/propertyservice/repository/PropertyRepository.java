package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.Property;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByHostId(Long hostId);
    List<Property> findByModerationStatus(String status); // F2 — admin lista pending

    // K7 fix — i city pretraga filtrira moderaciju (PENDING/REJECTED nisu javni)
    @Query("SELECT p FROM Property p WHERE p.city = :city AND p.isActive = true " +
           "AND (p.moderationStatus = 'APPROVED' OR p.moderationStatus IS NULL)")
    List<Property> findByCityAndIsActiveTrue(@Param("city") String city);

    // F2 — paged public listing samo APPROVED (ili legacy NULL za stari seed data)
    @Query("SELECT p FROM Property p WHERE p.moderationStatus = 'APPROVED' OR p.moderationStatus IS NULL")
    org.springframework.data.domain.Page<Property> findApprovedForPublic(org.springframework.data.domain.Pageable pageable);

    // BUG 4 — match po city OR country, case-insensitive, partial (LIKE)
    // K7 fix — /search je zaobilazio moderaciju: PENDING/REJECTED objekti su bili
    // vidljivi kroz pretragu po periodu iako ih javna lista skriva.
    @Query("SELECT p FROM Property p WHERE p.isActive = true AND " +
           "(p.moderationStatus = 'APPROVED' OR p.moderationStatus IS NULL) AND " +
           "(LOWER(p.city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(p.country) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "NOT EXISTS (SELECT b FROM CalendarBlock b WHERE b.property = p AND " +
           "b.startDate < :endDate AND b.endDate > :startDate)")
    List<Property> findAvailableProperties(@Param("q") String query,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * F18 — smjestaji unutar vidljivog dijela mape (bounding box).
     * Mapa poziva ovaj upit na svako pomicanje/zoom (dinamicko ucitavanje
     * iz dokumentacije) umjesto da sve drzi ucitano unaprijed.
     */
    @Query("SELECT p FROM Property p WHERE p.isActive = true " +
           "AND (p.moderationStatus = 'APPROVED' OR p.moderationStatus IS NULL) " +
           "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
           "AND p.latitude BETWEEN :minLat AND :maxLat " +
           "AND p.longitude BETWEEN :minLng AND :maxLng")
    List<Property> findInBounds(@Param("minLat") java.math.BigDecimal minLat,
                                @Param("maxLat") java.math.BigDecimal maxLat,
                                @Param("minLng") java.math.BigDecimal minLng,
                                @Param("maxLng") java.math.BigDecimal maxLng);

    /**
     * F11 — atomicni increment broja pregleda (bez read-modify-write race-a).
     * COALESCE jer postojece kolone iz starije seme mogu biti NULL.
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Property p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * EntityGraph za optimizaciju N+1 problema.
     * Učitava Property sa svim vezanim entitetima u jednom upitu.
     */
    @EntityGraph(attributePaths = {"images", "amenities", "pricingRule"})
    Optional<Property> findWithDetailsById(Long id);

    /**
     * EntityGraph za paginacioni upit sa amenities.
     */
    @EntityGraph(attributePaths = {"amenities"})
    @Query("SELECT p FROM Property p WHERE p.city = :city AND p.isActive = true")
    List<Property> findByCityWithAmenities(@Param("city") String city);
}
