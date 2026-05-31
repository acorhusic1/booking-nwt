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
    List<Property> findByCityAndIsActiveTrue(String city);
    List<Property> findByModerationStatus(String status); // F2 — admin lista pending

    // F2 — paged public listing samo APPROVED (ili legacy NULL za stari seed data)
    @Query("SELECT p FROM Property p WHERE p.moderationStatus = 'APPROVED' OR p.moderationStatus IS NULL")
    org.springframework.data.domain.Page<Property> findApprovedForPublic(org.springframework.data.domain.Pageable pageable);

    // BUG 4 — match po city OR country, case-insensitive, partial (LIKE)
    @Query("SELECT p FROM Property p WHERE p.isActive = true AND " +
           "(LOWER(p.city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(p.country) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "NOT EXISTS (SELECT b FROM CalendarBlock b WHERE b.property = p AND " +
           "b.startDate < :endDate AND b.endDate > :startDate)")
    List<Property> findAvailableProperties(@Param("q") String query,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

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
