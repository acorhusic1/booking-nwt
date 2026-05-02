package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.PromoCodeRequestDTO;
import com.bookingnwt.reservationservice.dto.PromoCodeResponseDTO;
import com.bookingnwt.reservationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.model.DiscountType;
import com.bookingnwt.reservationservice.service.PromoCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromoCodeController.class)
@Import(GlobalExceptionHandler.class)
class PromoCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromoCodeService promoCodeService;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private PromoCodeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
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
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPromoCode_Returns201() throws Exception {
        when(promoCodeService.createPromoCode(any(PromoCodeRequestDTO.class))).thenReturn(responseDTO);

        PromoCodeRequestDTO request = new PromoCodeRequestDTO();
        request.setCode("SUMMER2025");
        request.setDescription("Ljetnji popust");
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("15.00"));
        request.setMinNights(3);
        request.setValidFrom(LocalDate.of(2025, 6, 1));
        request.setValidTo(LocalDate.of(2025, 9, 30));
        request.setMaxUses(100);
        request.setCreatedBy(1L);

        mockMvc.perform(post("/api/promo-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("SUMMER2025"));
    }

    @Test
    void getPromoCode_Returns200() throws Exception {
        when(promoCodeService.getPromoCodeById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/promo-codes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPromoCode_NotFound() throws Exception {
        when(promoCodeService.getPromoCodeById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/promo-codes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByCode_Returns200() throws Exception {
        when(promoCodeService.getPromoCodeByCode("SUMMER2025")).thenReturn(responseDTO);

        mockMvc.perform(get("/api/promo-codes/code/SUMMER2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUMMER2025"));
    }

    @Test
    void getAllPromoCodes_Returns200() throws Exception {
        when(promoCodeService.getAllPromoCodes()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/promo-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updatePromoCode_Returns200() throws Exception {
        when(promoCodeService.updatePromoCode(eq(1L), any(PromoCodeRequestDTO.class))).thenReturn(responseDTO);

        PromoCodeRequestDTO request = new PromoCodeRequestDTO();
        request.setCode("SUMMER2025");
        request.setDescription("Ljetnji popust");
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("15.00"));
        request.setValidFrom(LocalDate.of(2025, 6, 1));
        request.setValidTo(LocalDate.of(2025, 9, 30));

        mockMvc.perform(put("/api/promo-codes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deletePromoCode_Returns204() throws Exception {
        doNothing().when(promoCodeService).deletePromoCode(1L);

        mockMvc.perform(delete("/api/promo-codes/1"))
                .andExpect(status().isNoContent());
    }
}
