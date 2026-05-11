package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'HOST')")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByReservationId(reservationId));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(paymentService.getPaymentsByGuestId(guestId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(@PathVariable Long id,
                                                                   @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    // === Task 4 — Non-trivial endpoints ===

    /** PATCH (RFC 6902) — partial update of a payment. */
    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDTO> patchPayment(@PathVariable Long id,
                                                            @RequestBody JsonNode patch) {
        return ResponseEntity.ok(paymentService.patchPayment(id, patch));
    }

    /** Pagination + sorting for guest's payment history. */
    @GetMapping("/guest/{guestId}/paged")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<Page<PaymentResponseDTO>> getByGuestPaged(@PathVariable Long guestId,
                                                                    Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByGuestPaged(guestId, pageable));
    }

    /** Custom @Query — total successfully completed spend for a guest. */
    @GetMapping("/guest/{guestId}/total-spent")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<BigDecimal> getTotalSpent(@PathVariable Long guestId) {
        return ResponseEntity.ok(paymentService.getTotalSpentByGuest(guestId));
    }

    /** Custom JPQL — payments by status above a min amount, ordered desc. */
    @GetMapping("/status/{status}/min/{minAmount}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDTO>> getByStatusAndMinAmount(
            @PathVariable PaymentStatus status,
            @PathVariable BigDecimal minAmount) {
        return ResponseEntity.ok(paymentService.findByStatusAndMinAmount(status, minAmount));
    }

    /** Stats — count payments by status. */
    @GetMapping("/status/{status}/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.countByStatus(status));
    }

    /** Batch insert — saveAll(). */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDTO>> batchCreate(
            @Valid @RequestBody List<PaymentRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.batchCreate(dtos));
    }

    /** EntityGraph fetch — payment + relatedPayment + walletTransactions in one query. */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<PaymentResponseDTO> getPaymentWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentWithDetails(id));
    }
}
