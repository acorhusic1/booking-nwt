package com.bookingnwt.userservice.service.impl;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.IdentityVerificationMapper;
import com.bookingnwt.userservice.model.IdentityVerification;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.repository.IdentityVerificationRepository;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.IdentityVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IdentityVerificationServiceImpl implements IdentityVerificationService {

    private final IdentityVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final IdentityVerificationMapper verificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<IdentityVerificationResponse> getVerificationsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Korisnik sa ID " + userId + " nije pronađen");
        }
        return verificationRepository.findByUserId(userId)
                .stream()
                .map(verificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IdentityVerificationResponse getVerificationById(Long id) {
        IdentityVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Verifikacija sa ID " + id + " nije pronađena"));
        return verificationMapper.toResponse(verification);
    }

    @Override
    public IdentityVerificationResponse createVerification(IdentityVerificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Korisnik sa ID " + request.getUserId() + " nije pronađen"));

        IdentityVerification verification = verificationMapper.toEntity(request);
        verification.setUser(user);
        verification.setStatus(com.bookingnwt.userservice.model.VerificationStatus.PENDING);
        verification.setSubmittedAt(java.time.LocalDateTime.now());

        IdentityVerification saved = verificationRepository.save(verification);
        return verificationMapper.toResponse(saved);
    }
}
