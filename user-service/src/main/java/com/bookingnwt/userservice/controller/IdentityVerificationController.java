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
