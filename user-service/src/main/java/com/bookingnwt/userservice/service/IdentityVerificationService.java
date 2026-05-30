package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;

import java.util.List;

public interface IdentityVerificationService {
    List<IdentityVerificationResponse> getVerificationsByUserId(Long userId);
    IdentityVerificationResponse getVerificationById(Long id);
    IdentityVerificationResponse createVerification(IdentityVerificationRequest request);
    // F16 — admin approve/reject
    List<IdentityVerificationResponse> getAllVerifications();
    IdentityVerificationResponse updateStatus(Long id, String status, Long verifiedBy);
}
