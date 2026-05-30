package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ProblemReportRequestDTO;
import com.bookingnwt.reservationservice.dto.ProblemReportResponseDTO;
import com.bookingnwt.reservationservice.model.ProblemReportStatus;
import com.bookingnwt.reservationservice.service.ProblemReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problem-reports")
@RequiredArgsConstructor
public class ProblemReportController {

    private final ProblemReportService reportService;

    @PostMapping
    public ResponseEntity<ProblemReportResponseDTO> createReport(@Valid @RequestBody ProblemReportRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemReportResponseDTO> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProblemReportResponseDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ProblemReportResponseDTO>> getByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reportService.getReportsByReservation(reservationId));
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<List<ProblemReportResponseDTO>> getByReporter(@PathVariable Long reporterId) {
        return ResponseEntity.ok(reportService.getReportsByReporter(reporterId));
    }

    // BUG 5 — Host vidi prijave problema za svoje smjestaje
    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<ProblemReportResponseDTO>> getByHost(@PathVariable Long hostId) {
        return ResponseEntity.ok(reportService.getReportsByHost(hostId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ProblemReportResponseDTO> updateStatus(@PathVariable Long id,
                                                                  @RequestParam ProblemReportStatus status) {
        return ResponseEntity.ok(reportService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
