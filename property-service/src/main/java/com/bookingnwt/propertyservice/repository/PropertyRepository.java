package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByHostId(Long hostId);
    List<Property> findByCityAndIsActiveTrue(String city);

    @Query("SELECT p FROM Property p WHERE p.city = :city AND p.isActive = true AND " +
           "NOT EXISTS (SELECT b FROM CalendarBlock b WHERE b.property = p AND " +
           "b.startDate < :endDate AND b.endDate > :startDate)")
    List<Property> findAvailableProperties(@Param("city") String city,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
