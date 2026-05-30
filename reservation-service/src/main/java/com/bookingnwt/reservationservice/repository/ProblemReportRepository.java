package com.bookingnwt.reservationservice.repository;

import com.bookingnwt.reservationservice.model.ProblemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemReportRepository extends JpaRepository<ProblemReport, Long> {
    List<ProblemReport> findByReservationId(Long reservationId);
    List<ProblemReport> findByReporterId(Long reporterId);

    // BUG 5 — Host vidi prijave problema za sve svoje rezervacije
    @Query("SELECT pr FROM ProblemReport pr WHERE pr.reservation.hostId = :hostId ORDER BY pr.reportedAt DESC")
    List<ProblemReport> findByReservationHostId(@Param("hostId") Long hostId);
}
