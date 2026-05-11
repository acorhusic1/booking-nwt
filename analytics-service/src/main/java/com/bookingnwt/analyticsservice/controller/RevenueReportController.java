package com.bookingnwt.analyticsservice.controller;

import com.bookingnwt.analyticsservice.dto.RevenueReportRequestDTO;
import com.bookingnwt.analyticsservice.dto.RevenueReportResponseDTO;
import com.bookingnwt.analyticsservice.service.RevenueReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class RevenueReportController {

    private final RevenueReportService reportService;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('HOST')")
    public ResponseEntity<RevenueReportResponseDTO> createReport(@Valid @RequestBody RevenueReportRequestDTO dto) {
        return new ResponseEntity<>(reportService.createReport(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RevenueReportResponseDTO> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueReportResponseDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/host/{hostId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<List<RevenueReportResponseDTO>> getReportsByHostId(@PathVariable Long hostId) {
        return ResponseEntity.ok(reportService.getReportsByHostId(hostId));
    }

    @GetMapping("/host/{hostId}/detailed")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<com.bookingnwt.analyticsservice.dto.DetailedHostReportDto> getDetailedHostReport(@PathVariable Long hostId) {
        return ResponseEntity.ok(reportService.getDetailedHostReport(hostId));
    }

    @GetMapping("/host/{hostId}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<List<RevenueReportResponseDTO>> getReportsByHostIdAndYear(
            @PathVariable Long hostId, @PathVariable Integer year) {
        return ResponseEntity.ok(reportService.getReportsByHostIdAndYear(hostId, year));
    }

    @GetMapping("/period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueReportResponseDTO>> getReportsByPeriod(
            @RequestParam Integer year, @RequestParam Integer month) {
        return ResponseEntity.ok(reportService.getReportsByPeriod(year, month));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
