package com.bookingnwt.analyticsservice.controller;

import com.bookingnwt.analyticsservice.dto.PropertyStatisticsRequestDTO;
import com.bookingnwt.analyticsservice.dto.PropertyStatisticsResponseDTO;
import com.bookingnwt.analyticsservice.service.PropertyStatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class PropertyStatisticsController {

    private final PropertyStatisticsService statisticsService;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyStatisticsResponseDTO> createStatistics(@Valid @RequestBody PropertyStatisticsRequestDTO dto) {
        return new ResponseEntity<>(statisticsService.createStatistics(dto), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyStatisticsResponseDTO>> createStatisticsBatch(@Valid @RequestBody List<PropertyStatisticsRequestDTO> dtos) {
        return new ResponseEntity<>(statisticsService.createStatisticsBatch(dtos), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PropertyStatisticsResponseDTO> getStatisticsById(@PathVariable Long id) {
        return ResponseEntity.ok(statisticsService.getStatisticsById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyStatisticsResponseDTO>> getAllStatistics() {
        return ResponseEntity.ok(statisticsService.getAllStatistics());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<PropertyStatisticsResponseDTO>> getAllStatisticsPaginated(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(statisticsService.getAllStatisticsPaginated(pageable));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<List<PropertyStatisticsResponseDTO>> getStatisticsByPropertyId(@PathVariable Long propertyId) {
        return ResponseEntity.ok(statisticsService.getStatisticsByPropertyId(propertyId));
    }

    @GetMapping("/host/{hostId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<List<PropertyStatisticsResponseDTO>> getStatisticsByHostId(@PathVariable Long hostId) {
        return ResponseEntity.ok(statisticsService.getStatisticsByHostId(hostId));
    }

    @GetMapping("/host/{hostId}/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<List<PropertyStatisticsResponseDTO>> getStatisticsByHostIdAndPeriod(
            @PathVariable Long hostId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ResponseEntity.ok(statisticsService.getStatisticsByHostIdAndPeriod(hostId, year, month));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStatistics(@PathVariable Long id) {
        statisticsService.deleteStatistics(id);
        return ResponseEntity.noContent().build();
    }
}
