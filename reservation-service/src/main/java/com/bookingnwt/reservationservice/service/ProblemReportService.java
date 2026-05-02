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
    ProblemReportResponseDTO updateStatus(Long id, ProblemReportStatus status);
    void deleteReport(Long id);
}
