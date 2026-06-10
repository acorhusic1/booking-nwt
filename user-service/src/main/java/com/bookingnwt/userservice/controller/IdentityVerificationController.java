package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;
import com.bookingnwt.userservice.service.IdentityVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/verifications")
@RequiredArgsConstructor
public class IdentityVerificationController {

    private final IdentityVerificationService verificationService;

    @GetMapping
    public ResponseEntity<List<IdentityVerificationResponse>> getVerifications(
            @PathVariable Long userId) {
        return ResponseEntity.ok(verificationService.getVerificationsByUserId(userId));
    }

    /**
     * F16 — "Verifikovan status domaćina je vidljiv gostima". Lagani javni
     * odgovor {verified: true/false} BEZ detalja dokumenta (privatnost) —
     * frontend prikazuje badge na stranici objekta.
     */
    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Boolean>> getVerifiedStatus(@PathVariable Long userId) {
        boolean verified = verificationService.getVerificationsByUserId(userId).stream()
                .anyMatch(v -> "APPROVED".equalsIgnoreCase(String.valueOf(v.getStatus())));
        return ResponseEntity.ok(java.util.Map.of("verified", verified));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IdentityVerificationResponse> getVerification(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.getVerificationById(id));
    }

    @PostMapping
    public ResponseEntity<IdentityVerificationResponse> createVerification(
            @PathVariable Long userId,
            @Valid @RequestBody IdentityVerificationRequest request) {
        request.setUserId(userId);
        IdentityVerificationResponse created = verificationService.createVerification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
