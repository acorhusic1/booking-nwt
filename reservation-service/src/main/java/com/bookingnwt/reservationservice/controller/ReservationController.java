package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.service.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
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

    /* ============================================================
     * K8 — ownership provjere. JwtAuthenticationFilter postavlja
     * "authUserId" atribut iz uid claima; ako atribut postoji,
     * korisnik smije raditi samo sa SVOJIM rezervacijama (osim ADMIN).
     * Legacy tokeni bez uid claima prolaze (token je potpisan pa se
     * claim ne moze falsifikovati — po novom loginu uid uvijek postoji).
     * ============================================================ */

    private Long authUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("authUserId");
        return uid instanceof Long l ? l : null;
    }

    /** Gost, host te rezervacije ili admin. */
    private void enforceParticipant(Long reservationId, HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) return;
        Long uid = authUserId(request);
        if (uid == null) return; // legacy token bez uid claima
        ReservationResponseDTO r = reservationService.getReservationById(reservationId);
        if (!uid.equals(r.getGuestId()) && !uid.equals(r.getHostId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Nemate pravo pristupa ovoj rezervaciji");
        }
    }

    /** Path {guestId}/{hostId} mora biti ulogovani korisnik (osim ADMIN). */
    private void enforceSelf(Long pathUserId, HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) return;
        Long uid = authUserId(request);
        if (uid != null && !uid.equals(pathUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Nemate pravo pristupa podacima drugog korisnika");
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO dto,
                                                                    HttpServletRequest request) {
        // Token-derivirani userId pregazi guestId iz body-a (sprjecava spoofing
        // tudjeg userId-a). Ako token nema uid claim (stari token bez ovog fielda),
        // padamo natrag na body — useri ce po novom loginu dobiti svjez token.
        Object authUid = request.getAttribute("authUserId");
        if (authUid instanceof Long uid) {
            dto.setGuestId(uid);
        }
        // 202 Accepted: rezervacija je kreirana sa status=CREATED, ali Saga
        // (naplata + property rezervacija) je jos u toku. Klijent dobija
        // konacan status (CONFIRMED / CANCELLED) kroz notifikaciju.
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reservationService.createReservation(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> getReservation(@PathVariable Long id,
                                                                 HttpServletRequest request) {
        enforceParticipant(id, request);
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByGuest(@PathVariable Long guestId,
                                                                   HttpServletRequest request) {
        enforceSelf(guestId, request);
        return ResponseEntity.ok(reservationService.getReservationsByGuest(guestId));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reservationService.getReservationsByProperty(propertyId));
    }

    /**
     * Public endpoint za prikaz zauzetih datuma na guest kalendaru.
     * Vraca samo {checkIn, checkOut, status} za aktivne rezervacije —
     * bez guest/host detalja. Svi authenticirani korisnici smiju (GUEST).
     */
    @GetMapping("/property/{propertyId}/occupied-dates")
    public ResponseEntity<List<java.util.Map<String, Object>>> getOccupiedDates(@PathVariable Long propertyId) {
        return ResponseEntity.ok(
                reservationService.getReservationsByProperty(propertyId).stream()
                        .filter(r -> {
                            String s = (r.getStatus() == null ? "" : r.getStatus().name()).toUpperCase();
                            return !"CANCELLED".equals(s);
                        })
                        .map(r -> {
                            java.util.Map<String, Object> m = new java.util.HashMap<>();
                            m.put("checkIn", r.getCheckIn());
                            m.put("checkOut", r.getCheckOut());
                            m.put("status", r.getStatus());
                            return m;
                        })
                        .toList()
        );
    }

    @GetMapping("/host/{hostId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByHost(@PathVariable Long hostId,
                                                                  HttpServletRequest request) {
        enforceSelf(hostId, request);
        return ResponseEntity.ok(reservationService.getReservationsByHost(hostId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ReservationResponseDTO> updateStatus(@PathVariable Long id,
                                                                @RequestParam ReservationStatus status,
                                                                HttpServletRequest request) {
        // K8 — host smije mijenjati status samo rezervacija za SVOJ smjestaj
        enforceParticipant(id, request);
        return ResponseEntity.ok(reservationService.updateStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@PathVariable Long id,
                                                                    HttpServletRequest request) {
        // K8 — bilo ko ulogovan je mogao otkazati TUDJU rezervaciju po ID-u
        enforceParticipant(id, request);
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PatchMapping("/{id}/is-cancelled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponseDTO> updateCancelStatus(
            @PathVariable Long id,
            @RequestParam Boolean isCancelled) {
        return ResponseEntity.ok(reservationService.updateCancelStatus(id, isCancelled));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    // === Task 4 — Non-trivial endpoints ===

    /** PATCH (RFC 6902 JSON Patch) — partial update of a reservation.
     *  K8 — samo ADMIN: patch moze mijenjati totalPrice/status pa ne smije
     *  biti dostupan gostima/hostovima. */
    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponseDTO> patchReservation(@PathVariable Long id,
                                                                    @RequestBody JsonNode patch) {
        return ResponseEntity.ok(reservationService.patchReservation(id, patch));
    }

    /** Pagination + sorting for guest's reservation history. */
    @GetMapping("/guest/{guestId}/paged")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<Page<ReservationResponseDTO>> getByGuestPaged(@PathVariable Long guestId,
                                                                        Pageable pageable,
                                                                        HttpServletRequest request) {
        enforceSelf(guestId, request);
        return ResponseEntity.ok(reservationService.getReservationsByGuestPaged(guestId, pageable));
    }

    /** Custom JPQL query — guest's reservations within a date range. */
    @GetMapping("/guest/{guestId}/range")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> getByGuestAndDateRange(
            @PathVariable Long guestId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        enforceSelf(guestId, request);
        return ResponseEntity.ok(reservationService.getReservationsByGuestAndDateRange(guestId, from, to));
    }

    /** Custom @Query — confirmed revenue for a host. */
    @GetMapping("/host/{hostId}/revenue")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<BigDecimal> getHostRevenue(@PathVariable Long hostId,
                                                     HttpServletRequest request) {
        enforceSelf(hostId, request);
        return ResponseEntity.ok(reservationService.getHostRevenue(hostId));
    }

    /** Batch insert — saveAll(). */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponseDTO>> batchCreate(
            @Valid @RequestBody List<ReservationRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.batchCreate(dtos));
    }

    /** EntityGraph fetch — reservation + cancellation policy + promo code + reports in one query. */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<ReservationResponseDTO> getReservationWithDetails(@PathVariable Long id,
                                                                            HttpServletRequest request) {
        enforceParticipant(id, request);
        return ResponseEntity.ok(reservationService.getReservationWithDetails(id));
    }
}
