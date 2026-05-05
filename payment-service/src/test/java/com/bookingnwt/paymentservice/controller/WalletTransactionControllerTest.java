package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.WalletTransactionResponseDTO;
import com.bookingnwt.paymentservice.exception.GlobalExceptionHandler;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.security.JwtAuthenticationFilter;
import com.bookingnwt.paymentservice.security.JwtTokenProvider;
import com.bookingnwt.paymentservice.service.WalletTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class WalletTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletTransactionService transactionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private WalletTransactionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new WalletTransactionResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setWalletId(1L);
        responseDTO.setAmount(new BigDecimal("100.00"));
        responseDTO.setType("DEPOSIT");
        responseDTO.setDescription("Test deposit");
    }

    @Test
    void getTransactionById_Returns200() throws Exception {
        when(transactionService.getTransactionById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("DEPOSIT"));
    }

    @Test
    void getTransactionById_NotFound_Returns404() throws Exception {
        when(transactionService.getTransactionById(99L))
                .thenThrow(new ResourceNotFoundException("Transakcija sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTransactions_Returns200() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTransactionsByWalletId_Returns200() throws Exception {
        when(transactionService.getTransactionsByWalletId(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/transactions/wallet/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTransactionsByPaymentId_Returns200() throws Exception {
        when(transactionService.getTransactionsByPaymentId(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/transactions/payment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
