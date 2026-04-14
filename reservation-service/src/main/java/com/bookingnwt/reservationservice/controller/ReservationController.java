package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<ReservationResponseDTO>> getByGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(reservationService.getReservationsByGuest(guestId));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReservationResponseDTO>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reservationService.getReservationsByProperty(propertyId));
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<ReservationResponseDTO>> getByHost(@PathVariable Long hostId) {
        return ResponseEntity.ok(reservationService.getReservationsByHost(hostId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponseDTO> updateStatus(@PathVariable Long id,
                                                                @RequestParam ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
