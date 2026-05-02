package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.IdentityVerificationMapper;
import com.bookingnwt.userservice.model.IdentityVerification;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.model.UserRole;
import com.bookingnwt.userservice.repository.IdentityVerificationRepository;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.impl.IdentityVerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceImplTest {

    @Mock
    private IdentityVerificationRepository verificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityVerificationMapper verificationMapper;

    @InjectMocks
    private IdentityVerificationServiceImpl verificationService;

    private User user;
    private IdentityVerification verification;
    private IdentityVerificationRequest request;
    private IdentityVerificationResponse response;

    @BeforeEach
    void setUp() {
        user = new User("test@email.com", "hash123", "Ivo", "Ivić", "+38761111111", UserRole.GUEST);
        user.setId(1L);

        verification = new IdentityVerification(user, "LIČNA KARTA", "123456789");
        verification.setId(1L);

        request = new IdentityVerificationRequest(1L, "LIČNA KARTA", "123456789");

        response = new IdentityVerificationResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setDocumentType("LIČNA KARTA");
        response.setDocumentNumber("123456789");
        response.setStatus("PENDING");
        response.setSubmittedAt(LocalDateTime.now());
    }

    @Test
    void getVerificationsByUserId_shouldReturnList_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(verificationRepository.findByUserId(1L)).thenReturn(List.of(verification));
        when(verificationMapper.toResponse(verification)).thenReturn(response);

        List<IdentityVerificationResponse> result = verificationService.getVerificationsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentType()).isEqualTo("LIČNA KARTA");
    }

    @Test
    void getVerificationsByUserId_shouldThrow_whenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> verificationService.getVerificationsByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getVerificationById_shouldReturnVerification_whenExists() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(verification));
        when(verificationMapper.toResponse(verification)).thenReturn(response);

        IdentityVerificationResponse result = verificationService.getVerificationById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getVerificationById_shouldThrow_whenNotFound() {
        when(verificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.getVerificationById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createVerification_shouldSaveAndReturn() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(verificationMapper.toEntity(request)).thenReturn(verification);
        when(verificationRepository.save(any(IdentityVerification.class))).thenReturn(verification);
        when(verificationMapper.toResponse(verification)).thenReturn(response);

        IdentityVerificationResponse result = verificationService.createVerification(request);

        assertThat(result.getDocumentType()).isEqualTo("LIČNA KARTA");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(verificationRepository).save(any(IdentityVerification.class));
    }

    @Test
    void createVerification_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        request.setUserId(99L);

        assertThatThrownBy(() -> verificationService.createVerification(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
