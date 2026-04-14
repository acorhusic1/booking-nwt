package com.bookingnwt.reservationservice.repository;

import com.bookingnwt.reservationservice.model.ProblemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemReportRepository extends JpaRepository<ProblemReport, Long> {
    List<ProblemReport> findByReservationId(Long reservationId);
    List<ProblemReport> findByReporterId(Long reporterId);
}
