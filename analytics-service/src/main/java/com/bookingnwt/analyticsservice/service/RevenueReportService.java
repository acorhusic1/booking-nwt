package com.bookingnwt.analyticsservice.service;

import com.bookingnwt.analyticsservice.dto.RevenueReportRequestDTO;
import com.bookingnwt.analyticsservice.dto.RevenueReportResponseDTO;

import java.util.List;

public interface RevenueReportService {

    RevenueReportResponseDTO createReport(RevenueReportRequestDTO dto);

    RevenueReportResponseDTO getReportById(Long id);

    List<RevenueReportResponseDTO> getAllReports();

    List<RevenueReportResponseDTO> getReportsByHostId(Long hostId);

    com.bookingnwt.analyticsservice.dto.DetailedHostReportDto getDetailedHostReport(Long hostId);

    List<RevenueReportResponseDTO> getReportsByHostIdAndYear(Long hostId, Integer year);

    List<RevenueReportResponseDTO> getReportsByPeriod(Integer year, Integer month);

    void deleteReport(Long id);
}
