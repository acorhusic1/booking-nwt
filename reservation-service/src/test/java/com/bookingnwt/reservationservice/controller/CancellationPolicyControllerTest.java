package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.CancellationPolicyRequestDTO;
import com.bookingnwt.reservationservice.dto.CancellationPolicyResponseDTO;
import com.bookingnwt.reservationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.security.JwtAuthenticationFilter;
import com.bookingnwt.reservationservice.security.JwtTokenProvider;
import com.bookingnwt.reservationservice.service.CancellationPolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CancellationPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CancellationPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CancellationPolicyService policyService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private CancellationPolicyResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new CancellationPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyId(100L);
        responseDTO.setName("Fleksibilna");
        responseDTO.setFreeCancelDays(7);
        responseDTO.setPartialRefundPct(50);
        responseDTO.setNoRefund(false);
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPolicy_Returns201() throws Exception {
        when(policyService.createPolicy(any(CancellationPolicyRequestDTO.class))).thenReturn(responseDTO);

        CancellationPolicyRequestDTO request = new CancellationPolicyRequestDTO();
        request.setPropertyId(100L);
        request.setName("Fleksibilna");
        request.setFreeCancelDays(7);
        request.setPartialRefundPct(50);
        request.setNoRefund(false);

        mockMvc.perform(post("/api/cancellation-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fleksibilna"));
    }

    @Test
    void getPolicy_Returns200() throws Exception {
        when(policyService.getPolicyById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/cancellation-policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPolicy_NotFound() throws Exception {
        when(policyService.getPolicyById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/cancellation-policies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPolicies_Returns200() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/cancellation-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByProperty_Returns200() throws Exception {
        when(policyService.getPoliciesByProperty(100L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/cancellation-policies/property/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updatePolicy_Returns200() throws Exception {
        when(policyService.updatePolicy(eq(1L), any(CancellationPolicyRequestDTO.class))).thenReturn(responseDTO);

        CancellationPolicyRequestDTO request = new CancellationPolicyRequestDTO();
        request.setPropertyId(100L);
        request.setName("Fleksibilna");
        request.setFreeCancelDays(7);
        request.setPartialRefundPct(50);
        request.setNoRefund(false);

        mockMvc.perform(put("/api/cancellation-policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deletePolicy_Returns204() throws Exception {
        doNothing().when(policyService).deletePolicy(1L);

        mockMvc.perform(delete("/api/cancellation-policies/1"))
                .andExpect(status().isNoContent());
    }
}
