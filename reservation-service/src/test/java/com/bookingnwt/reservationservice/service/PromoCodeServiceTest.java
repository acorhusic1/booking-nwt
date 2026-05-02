package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.PromoCodeRequestDTO;
import com.bookingnwt.reservationservice.dto.PromoCodeResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.PromoCodeMapper;
import com.bookingnwt.reservationservice.model.DiscountType;
import com.bookingnwt.reservationservice.model.PromoCode;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.service.impl.PromoCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private PromoCodeMapper promoCodeMapper;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    private PromoCode promoCode;
    private PromoCodeRequestDTO requestDTO;
    private PromoCodeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        promoCode = new PromoCode();
        promoCode.setId(1L);
        promoCode.setCode("SUMMER2025");
        promoCode.setDescription("Ljetnji popust");
        promoCode.setDiscountType(DiscountType.PERCENTAGE);
        promoCode.setDiscountValue(new BigDecimal("15.00"));
        promoCode.setMinNights(3);
        promoCode.setValidFrom(LocalDate.of(2025, 6, 1));
        promoCode.setValidTo(LocalDate.of(2025, 9, 30));
        promoCode.setMaxUses(100);
        promoCode.setUsageCount(0);
        promoCode.setCreatedBy(1L);
        promoCode.setCreatedAt(LocalDateTime.now());

        requestDTO = new PromoCodeRequestDTO();
        requestDTO.setCode("SUMMER2025");
        requestDTO.setDescription("Ljetnji popust");
        requestDTO.setDiscountType(DiscountType.PERCENTAGE);
        requestDTO.setDiscountValue(new BigDecimal("15.00"));
        requestDTO.setMinNights(3);
        requestDTO.setValidFrom(LocalDate.of(2025, 6, 1));
        requestDTO.setValidTo(LocalDate.of(2025, 9, 30));
        requestDTO.setMaxUses(100);
        requestDTO.setCreatedBy(1L);

        responseDTO = new PromoCodeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setCode("SUMMER2025");
        responseDTO.setDescription("Ljetnji popust");
        responseDTO.setDiscountType(DiscountType.PERCENTAGE);
        responseDTO.setDiscountValue(new BigDecimal("15.00"));
        responseDTO.setMinNights(3);
        responseDTO.setValidFrom(LocalDate.of(2025, 6, 1));
        responseDTO.setValidTo(LocalDate.of(2025, 9, 30));
        responseDTO.setMaxUses(100);
        responseDTO.setUsageCount(0);
        responseDTO.setCreatedBy(1L);
    }

    @Test
    void createPromoCode_Success() {
        when(promoCodeMapper.toEntity(requestDTO)).thenReturn(promoCode);
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCode);
        when(promoCodeMapper.toResponseDTO(promoCode)).thenReturn(responseDTO);

        PromoCodeResponseDTO result = promoCodeService.createPromoCode(requestDTO);

        assertNotNull(result);
        assertEquals("SUMMER2025", result.getCode());
        verify(promoCodeRepository).save(any(PromoCode.class));
    }

    @Test
    void getPromoCodeById_Success() {
        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCode));
        when(promoCodeMapper.toResponseDTO(promoCode)).thenReturn(responseDTO);

        PromoCodeResponseDTO result = promoCodeService.getPromoCodeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPromoCodeById_NotFound() {
        when(promoCodeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> promoCodeService.getPromoCodeById(99L));
    }

    @Test
    void getPromoCodeByCode_Success() {
        when(promoCodeRepository.findByCode("SUMMER2025")).thenReturn(Optional.of(promoCode));
        when(promoCodeMapper.toResponseDTO(promoCode)).thenReturn(responseDTO);

        PromoCodeResponseDTO result = promoCodeService.getPromoCodeByCode("SUMMER2025");

        assertNotNull(result);
        assertEquals("SUMMER2025", result.getCode());
    }

    @Test
    void getPromoCodeByCode_NotFound() {
        when(promoCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> promoCodeService.getPromoCodeByCode("INVALID"));
    }

    @Test
    void getAllPromoCodes_Success() {
        when(promoCodeRepository.findAll()).thenReturn(List.of(promoCode));
        when(promoCodeMapper.toResponseDTO(promoCode)).thenReturn(responseDTO);

        List<PromoCodeResponseDTO> result = promoCodeService.getAllPromoCodes();

        assertEquals(1, result.size());
    }

    @Test
    void updatePromoCode_Success() {
        when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(promoCode));
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCode);
        when(promoCodeMapper.toResponseDTO(promoCode)).thenReturn(responseDTO);

        PromoCodeResponseDTO result = promoCodeService.updatePromoCode(1L, requestDTO);

        assertNotNull(result);
        verify(promoCodeRepository).save(any(PromoCode.class));
    }

    @Test
    void deletePromoCode_Success() {
        when(promoCodeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(promoCodeRepository).deleteById(1L);

        assertDoesNotThrow(() -> promoCodeService.deletePromoCode(1L));
        verify(promoCodeRepository).deleteById(1L);
    }

    @Test
    void deletePromoCode_NotFound() {
        when(promoCodeRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> promoCodeService.deletePromoCode(99L));
    }
}
