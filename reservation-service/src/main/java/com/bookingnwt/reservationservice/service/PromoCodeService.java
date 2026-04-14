package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.PromoCodeRequestDTO;
import com.bookingnwt.reservationservice.dto.PromoCodeResponseDTO;

import java.util.List;

public interface PromoCodeService {
    PromoCodeResponseDTO createPromoCode(PromoCodeRequestDTO dto);
    PromoCodeResponseDTO getPromoCodeById(Long id);
    PromoCodeResponseDTO getPromoCodeByCode(String code);
    List<PromoCodeResponseDTO> getAllPromoCodes();
    PromoCodeResponseDTO updatePromoCode(Long id, PromoCodeRequestDTO dto);
    void deletePromoCode(Long id);
}
