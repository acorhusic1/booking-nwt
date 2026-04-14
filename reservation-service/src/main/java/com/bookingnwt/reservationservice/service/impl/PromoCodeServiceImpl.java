package com.bookingnwt.reservationservice.service.impl;

import com.bookingnwt.reservationservice.dto.PromoCodeRequestDTO;
import com.bookingnwt.reservationservice.dto.PromoCodeResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.PromoCodeMapper;
import com.bookingnwt.reservationservice.model.PromoCode;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Override
    public PromoCodeResponseDTO createPromoCode(PromoCodeRequestDTO dto) {
        PromoCode promoCode = promoCodeMapper.toEntity(dto);
        promoCode.setUsageCount(0);
        promoCode.setCreatedAt(LocalDateTime.now());
        PromoCode saved = promoCodeRepository.save(promoCode);
        return promoCodeMapper.toResponseDTO(saved);
    }

    @Override
    public PromoCodeResponseDTO getPromoCodeById(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode nije pronađen sa ID: " + id));
        return promoCodeMapper.toResponseDTO(promoCode);
    }

    @Override
    public PromoCodeResponseDTO getPromoCodeByCode(String code) {
        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode nije pronađen sa kodom: " + code));
        return promoCodeMapper.toResponseDTO(promoCode);
    }

    @Override
    public List<PromoCodeResponseDTO> getAllPromoCodes() {
        return promoCodeRepository.findAll().stream()
                .map(promoCodeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PromoCodeResponseDTO updatePromoCode(Long id, PromoCodeRequestDTO dto) {
        PromoCode existing = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode nije pronađen sa ID: " + id));
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setDiscountType(dto.getDiscountType());
        existing.setDiscountValue(dto.getDiscountValue());
        existing.setMinNights(dto.getMinNights());
        existing.setValidFrom(dto.getValidFrom());
        existing.setValidTo(dto.getValidTo());
        existing.setMaxUses(dto.getMaxUses());
        existing.setCreatedBy(dto.getCreatedBy());
        PromoCode saved = promoCodeRepository.save(existing);
        return promoCodeMapper.toResponseDTO(saved);
    }

    @Override
    public void deletePromoCode(Long id) {
        if (!promoCodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("PromoCode nije pronađen sa ID: " + id);
        }
        promoCodeRepository.deleteById(id);
    }
}
