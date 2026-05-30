package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.IdentityVerificationResponse;
import com.bookingnwt.userservice.service.IdentityVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * F16 — Admin pregled i odobravanje verifikacija identiteta domaćina.
 * Odvojen od /api/users/{userId}/verifications jer admin radi cross-user
 * (ne zna userId unaprijed).
 */
@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
public class VerificationAdminController {

    private final IdentityVerificationService verificationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IdentityVerificationResponse>> getAll() {
        return ResponseEntity.ok(verificationService.getAllVerifications());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IdentityVerificationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long verifiedBy) {
        return ResponseEntity.ok(verificationService.updateStatus(id, status, verifiedBy));
    }
}
