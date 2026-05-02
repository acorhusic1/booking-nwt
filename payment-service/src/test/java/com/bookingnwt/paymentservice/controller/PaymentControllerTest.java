package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.exception.GlobalExceptionHandler;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private ObjectMapper objectMapper;
    private PaymentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new PaymentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setReservationId(1L);
        responseDTO.setGuestId(4L);
        responseDTO.setAmount(new BigDecimal("200.00"));
        responseDTO.setCurrency("BAM");
        responseDTO.setStatus("PENDING");
        responseDTO.setMethod("WALLET");
    }

    @Test
    void createPayment_Returns201() throws Exception {
        when(paymentService.createPayment(any(PaymentRequestDTO.class))).thenReturn(responseDTO);

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setReservationId(1L);
        requestDTO.setGuestId(4L);
        requestDTO.setAmount(new BigDecimal("200.00"));
        requestDTO.setCurrency("BAM");
        requestDTO.setMethod("WALLET");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createPayment_InvalidBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentById_Returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPaymentById_NotFound_Returns404() throws Exception {
        when(paymentService.getPaymentById(99L)).thenThrow(new ResourceNotFoundException("Plaćanje sa ID 99 nije pronađeno"));

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPayments_Returns200() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPaymentsByReservationId_Returns200() throws Exception {
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/payments/reservation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPaymentsByGuestId_Returns200() throws Exception {
        when(paymentService.getPaymentsByGuestId(4L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/payments/guest/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updatePaymentStatus_Returns200() throws Exception {
        PaymentResponseDTO completedDTO = new PaymentResponseDTO();
        completedDTO.setId(1L);
        completedDTO.setStatus("COMPLETED");
        when(paymentService.updatePaymentStatus(eq(1L), eq(PaymentStatus.COMPLETED))).thenReturn(completedDTO);

        mockMvc.perform(put("/api/payments/1/status?status=COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void refundPayment_Returns200() throws Exception {
        PaymentResponseDTO refundDTO = new PaymentResponseDTO();
        refundDTO.setId(2L);
        refundDTO.setStatus("REFUNDED");
        refundDTO.setRelatedPaymentId(1L);
        when(paymentService.refundPayment(1L)).thenReturn(refundDTO);

        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void deletePayment_Returns204() throws Exception {
        doNothing().when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isNoContent());
    }
}
