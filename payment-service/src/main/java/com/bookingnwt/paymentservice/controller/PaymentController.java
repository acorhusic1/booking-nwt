package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByReservationId(reservationId));
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(paymentService.getPaymentsByGuestId(guestId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(@PathVariable Long id,
                                                                   @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
