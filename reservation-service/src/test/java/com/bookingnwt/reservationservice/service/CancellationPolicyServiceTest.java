package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.CancellationPolicyRequestDTO;
import com.bookingnwt.reservationservice.dto.CancellationPolicyResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.CancellationPolicyMapper;
import com.bookingnwt.reservationservice.model.CancellationPolicy;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.service.impl.CancellationPolicyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationPolicyServiceTest {

    @Mock
    private CancellationPolicyRepository policyRepository;
    @Mock
    private CancellationPolicyMapper policyMapper;

    @InjectMocks
    private CancellationPolicyServiceImpl policyService;

    private CancellationPolicy policy;
    private CancellationPolicyRequestDTO requestDTO;
    private CancellationPolicyResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        policy = new CancellationPolicy();
        policy.setId(1L);
        policy.setPropertyId(100L);
        policy.setName("Fleksibilna");
        policy.setFreeCancelDays(7);
        policy.setPartialRefundPct(50);
        policy.setNoRefund(false);
        policy.setCreatedAt(LocalDateTime.now());

        requestDTO = new CancellationPolicyRequestDTO();
        requestDTO.setPropertyId(100L);
        requestDTO.setName("Fleksibilna");
        requestDTO.setFreeCancelDays(7);
        requestDTO.setPartialRefundPct(50);
        requestDTO.setNoRefund(false);

        responseDTO = new CancellationPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyId(100L);
        responseDTO.setName("Fleksibilna");
        responseDTO.setFreeCancelDays(7);
        responseDTO.setPartialRefundPct(50);
        responseDTO.setNoRefund(false);
    }

    @Test
    void createPolicy_Success() {
        when(policyMapper.toEntity(requestDTO)).thenReturn(policy);
        when(policyRepository.save(any(CancellationPolicy.class))).thenReturn(policy);
        when(policyMapper.toResponseDTO(policy)).thenReturn(responseDTO);

        CancellationPolicyResponseDTO result = policyService.createPolicy(requestDTO);

        assertNotNull(result);
        assertEquals("Fleksibilna", result.getName());
        verify(policyRepository).save(any(CancellationPolicy.class));
    }

    @Test
    void getPolicyById_Success() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyMapper.toResponseDTO(policy)).thenReturn(responseDTO);

        CancellationPolicyResponseDTO result = policyService.getPolicyById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPolicyById_NotFound() {
        when(policyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> policyService.getPolicyById(99L));
    }

    @Test
    void getAllPolicies_Success() {
        when(policyRepository.findAll()).thenReturn(List.of(policy));
        when(policyMapper.toResponseDTO(policy)).thenReturn(responseDTO);

        List<CancellationPolicyResponseDTO> result = policyService.getAllPolicies();

        assertEquals(1, result.size());
    }

    @Test
    void getPoliciesByProperty_Success() {
        when(policyRepository.findByPropertyId(100L)).thenReturn(List.of(policy));
        when(policyMapper.toResponseDTO(policy)).thenReturn(responseDTO);

        List<CancellationPolicyResponseDTO> result = policyService.getPoliciesByProperty(100L);

        assertEquals(1, result.size());
    }

    @Test
    void updatePolicy_Success() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(CancellationPolicy.class))).thenReturn(policy);
        when(policyMapper.toResponseDTO(policy)).thenReturn(responseDTO);

        CancellationPolicyResponseDTO result = policyService.updatePolicy(1L, requestDTO);

        assertNotNull(result);
        verify(policyRepository).save(any(CancellationPolicy.class));
    }

    @Test
    void deletePolicy_Success() {
        when(policyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(policyRepository).deleteById(1L);

        assertDoesNotThrow(() -> policyService.deletePolicy(1L));
        verify(policyRepository).deleteById(1L);
    }

    @Test
    void deletePolicy_NotFound() {
        when(policyRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> policyService.deletePolicy(99L));
    }
}
