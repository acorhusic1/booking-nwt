package com.bookingnwt.reservationservice.service.impl;

import com.bookingnwt.reservationservice.dto.CancellationPolicyRequestDTO;
import com.bookingnwt.reservationservice.dto.CancellationPolicyResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.CancellationPolicyMapper;
import com.bookingnwt.reservationservice.model.CancellationPolicy;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.service.CancellationPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancellationPolicyServiceImpl implements CancellationPolicyService {

    private final CancellationPolicyRepository policyRepository;
    private final CancellationPolicyMapper policyMapper;

    @Override
    public CancellationPolicyResponseDTO createPolicy(CancellationPolicyRequestDTO dto) {
        CancellationPolicy policy = policyMapper.toEntity(dto);
        policy.setCreatedAt(LocalDateTime.now());
        CancellationPolicy saved = policyRepository.save(policy);
        return policyMapper.toResponseDTO(saved);
    }

    @Override
    public CancellationPolicyResponseDTO getPolicyById(Long id) {
        CancellationPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy nije pronađen sa ID: " + id));
        return policyMapper.toResponseDTO(policy);
    }

    @Override
    public List<CancellationPolicyResponseDTO> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CancellationPolicyResponseDTO> getPoliciesByProperty(Long propertyId) {
        return policyRepository.findByPropertyId(propertyId).stream()
                .map(policyMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CancellationPolicyResponseDTO updatePolicy(Long id, CancellationPolicyRequestDTO dto) {
        CancellationPolicy existing = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy nije pronađen sa ID: " + id));
        existing.setPropertyId(dto.getPropertyId());
        existing.setName(dto.getName());
        existing.setFreeCancelDays(dto.getFreeCancelDays());
        existing.setPartialRefundPct(dto.getPartialRefundPct());
        existing.setNoRefund(dto.getNoRefund());
        CancellationPolicy saved = policyRepository.save(existing);
        return policyMapper.toResponseDTO(saved);
    }

    @Override
    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("CancellationPolicy nije pronađen sa ID: " + id);
        }
        policyRepository.deleteById(id);
    }
}
