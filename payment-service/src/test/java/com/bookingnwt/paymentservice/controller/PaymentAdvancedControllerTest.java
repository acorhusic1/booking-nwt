package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.exception.GlobalExceptionHandler;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.security.JwtAuthenticationFilter;
import com.bookingnwt.paymentservice.security.JwtTokenProvider;
import com.bookingnwt.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PaymentAdvancedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private PaymentResponseDTO sample;

    @BeforeEach
    void setUp() {
        sample = new PaymentResponseDTO();
        sample.setId(1L);
        sample.setReservationId(10L);
        sample.setGuestId(4L);
        sample.setAmount(new BigDecimal("200.00"));
        sample.setCurrency("BAM");
        sample.setMethod("WALLET");
        sample.setStatus("COMPLETED");
    }

    @Test
    void patchPayment_Returns200() throws Exception {
        when(paymentService.patchPayment(eq(1L), any(JsonNode.class))).thenReturn(sample);

        String patch = "[{\"op\":\"replace\",\"path\":\"/method\",\"value\":\"CARD\"}]";
        mockMvc.perform(patch("/api/payments/1")
                        .contentType("application/json-patch+json")
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patchPayment_InvalidPatch_Returns400() throws Exception {
        when(paymentService.patchPayment(eq(1L), any(JsonNode.class)))
                .thenThrow(new IllegalArgumentException("Neispravna JSON Patch operacija"));

        mockMvc.perform(patch("/api/payments/1")
                        .contentType("application/json-patch+json")
                        .content("[{\"op\":\"bogus\"}]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentsByGuestPaged_Returns200() throws Exception {
        Page<PaymentResponseDTO> page = new PageImpl<>(List.of(sample));
        when(paymentService.getPaymentsByGuestPaged(eq(4L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/payments/guest/4/paged?page=0&size=5&sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getTotalSpent_Returns200() throws Exception {
        when(paymentService.getTotalSpentByGuest(4L)).thenReturn(new BigDecimal("999.99"));

        mockMvc.perform(get("/api/payments/guest/4/total-spent"))
                .andExpect(status().isOk())
                .andExpect(content().string("999.99"));
    }

    @Test
    void getByStatusAndMinAmount_Returns200() throws Exception {
        when(paymentService.findByStatusAndMinAmount(eq(PaymentStatus.COMPLETED), eq(new BigDecimal("100"))))
                .thenReturn(List.of(sample));

        mockMvc.perform(get("/api/payments/status/COMPLETED/min/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByStatusAndMinAmount_BadInput_Returns400() throws Exception {
        when(paymentService.findByStatusAndMinAmount(any(PaymentStatus.class), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Minimalni iznos mora biti >= 0"));

        mockMvc.perform(get("/api/payments/status/COMPLETED/min/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void countByStatus_Returns200() throws Exception {
        when(paymentService.countByStatus(PaymentStatus.PENDING)).thenReturn(7L);

        mockMvc.perform(get("/api/payments/status/PENDING/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void batchCreate_Returns201() throws Exception {
        when(paymentService.batchCreate(anyList())).thenReturn(List.of(sample, sample));

        PaymentRequestDTO req = new PaymentRequestDTO();
        req.setReservationId(10L);
        req.setGuestId(4L);
        req.setAmount(new BigDecimal("200.00"));
        req.setCurrency("BAM");
        req.setMethod("WALLET");

        mockMvc.perform(post("/api/payments/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(req, req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPaymentWithDetails_Returns200() throws Exception {
        when(paymentService.getPaymentWithDetails(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/payments/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
