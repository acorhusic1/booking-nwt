package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ProblemReportRequestDTO;
import com.bookingnwt.reservationservice.dto.ProblemReportResponseDTO;
import com.bookingnwt.reservationservice.model.ProblemReportStatus;

import java.util.List;

public interface ProblemReportService {
    ProblemReportResponseDTO createReport(ProblemReportRequestDTO dto);
    ProblemReportResponseDTO getReportById(Long id);
    List<ProblemReportResponseDTO> getAllReports();
    List<ProblemReportResponseDTO> getReportsByReservation(Long reservationId);
    List<ProblemReportResponseDTO> getReportsByReporter(Long reporterId);
    // BUG 5 — Host vidi sve prijave za svoje smještaje
    List<ProblemReportResponseDTO> getReportsByHost(Long hostId);
    ProblemReportResponseDTO updateStatus(Long id, ProblemReportStatus status);
    void deleteReport(Long id);
}
