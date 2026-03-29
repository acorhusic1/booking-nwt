package com.bookingnwt.analyticsservice.repository;

import com.bookingnwt.analyticsservice.model.RevenueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevenueReportRepository extends JpaRepository<RevenueReport, Long> {
    List<RevenueReport> findByHostId(Long hostId);
    List<RevenueReport> findByHostIdAndYear(Long hostId, Integer year);
    List<RevenueReport> findByYearAndMonth(Integer year, Integer month);
}
