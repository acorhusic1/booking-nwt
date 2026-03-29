package com.bookingnwt.analyticsservice.repository;

import com.bookingnwt.analyticsservice.model.PropertyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyStatisticsRepository extends JpaRepository<PropertyStatistics, Long> {
    List<PropertyStatistics> findByHostId(Long hostId);
    List<PropertyStatistics> findByPropertyId(Long propertyId);
    List<PropertyStatistics> findByHostIdAndYearAndMonth(Long hostId, Integer year, Integer month);
}
