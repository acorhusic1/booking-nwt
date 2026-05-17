package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.service.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(reservationService.getReservationsByGuest(guestId));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasAnyAuthority('HOST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reservationService.getReservationsByProperty(propertyId));
    }

    @GetMapping("/host/{hostId}")
    @PreAuthorize("hasAnyAuthority('HOST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByHost(@PathVariable Long hostId) {
        return ResponseEntity.ok(reservationService.getReservationsByHost(hostId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('HOST', 'ADMIN')")
    public ResponseEntity<ReservationResponseDTO> updateStatus(@PathVariable Long id,
                                                                @RequestParam ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    // === Task 4 — Non-trivial endpoints ===

    /** PATCH (RFC 6902 JSON Patch) — partial update of a reservation. */
    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> patchReservation(@PathVariable Long id,
                                                                    @RequestBody JsonNode patch) {
        return ResponseEntity.ok(reservationService.patchReservation(id, patch));
    }

    /** Pagination + sorting for guest's reservation history. */
    @GetMapping("/guest/{guestId}/paged")
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN')")
    public ResponseEntity<Page<ReservationResponseDTO>> getByGuestPaged(@PathVariable Long guestId,
                                                                        Pageable pageable) {
        return ResponseEntity.ok(reservationService.getReservationsByGuestPaged(guestId, pageable));
    }

    /** Custom JPQL query — guest's reservations within a date range. */
    @GetMapping("/guest/{guestId}/range")
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByGuestAndDateRange(
            @PathVariable Long guestId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reservationService.getReservationsByGuestAndDateRange(guestId, from, to));
    }

    /** Custom @Query — confirmed revenue for a host. */
    @GetMapping("/host/{hostId}/revenue")
    @PreAuthorize("hasAnyAuthority('HOST', 'ADMIN')")
    public ResponseEntity<BigDecimal> getHostRevenue(@PathVariable Long hostId) {
        return ResponseEntity.ok(reservationService.getHostRevenue(hostId));
    }

    /** Batch insert — saveAll(). */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> batchCreate(
            @Valid @RequestBody List<ReservationRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.batchCreate(dtos));
    }

    /** EntityGraph fetch — reservation + cancellation policy + promo code + reports in one query. */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> getReservationWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationWithDetails(id));
    }
}
