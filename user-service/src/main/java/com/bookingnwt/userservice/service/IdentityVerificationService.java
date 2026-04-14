package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;

import java.util.List;

public interface IdentityVerificationService {
    List<IdentityVerificationResponse> getVerificationsByUserId(Long userId);
    IdentityVerificationResponse getVerificationById(Long id);
    IdentityVerificationResponse createVerification(IdentityVerificationRequest request);
}
