package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.CancellationPolicyRequestDTO;
import com.bookingnwt.reservationservice.dto.CancellationPolicyResponseDTO;

import java.util.List;

public interface CancellationPolicyService {
    CancellationPolicyResponseDTO createPolicy(CancellationPolicyRequestDTO dto);
    CancellationPolicyResponseDTO getPolicyById(Long id);
    List<CancellationPolicyResponseDTO> getAllPolicies();
    List<CancellationPolicyResponseDTO> getPoliciesByProperty(Long propertyId);
    CancellationPolicyResponseDTO updatePolicy(Long id, CancellationPolicyRequestDTO dto);
    void deletePolicy(Long id);
}
