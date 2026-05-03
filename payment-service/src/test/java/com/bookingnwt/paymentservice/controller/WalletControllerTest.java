package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.WalletRequestDTO;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;
import com.bookingnwt.paymentservice.exception.GlobalExceptionHandler;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.security.JwtAuthenticationFilter;
import com.bookingnwt.paymentservice.security.JwtTokenProvider;
import com.bookingnwt.paymentservice.service.WalletService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;
    private WalletResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new WalletResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(10L);
        responseDTO.setBalance(new BigDecimal("500.00"));
        responseDTO.setCurrency("BAM");
    }

    @Test
    void createWallet_Returns201() throws Exception {
        when(walletService.createWallet(any(WalletRequestDTO.class))).thenReturn(responseDTO);

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setUserId(10L);
        requestDTO.setBalance(new BigDecimal("500.00"));
        requestDTO.setCurrency("BAM");

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void createWallet_InvalidBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWalletById_Returns200() throws Exception {
        when(walletService.getWalletById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/wallets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getWalletById_NotFound_Returns404() throws Exception {
        when(walletService.getWalletById(99L)).thenThrow(new ResourceNotFoundException("Novčanik sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/wallets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWalletByUserId_Returns200() throws Exception {
        when(walletService.getWalletByUserId(10L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/wallets/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void getAllWallets_Returns200() throws Exception {
        when(walletService.getAllWallets()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deposit_Returns200() throws Exception {
        WalletResponseDTO depositedDTO = new WalletResponseDTO();
        depositedDTO.setId(1L);
        depositedDTO.setBalance(new BigDecimal("600.00"));
        when(walletService.deposit(eq(1L), any(BigDecimal.class))).thenReturn(depositedDTO);

        mockMvc.perform(post("/api/wallets/1/deposit?amount=100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(600.00));
    }

    @Test
    void withdraw_Returns200() throws Exception {
        WalletResponseDTO withdrawnDTO = new WalletResponseDTO();
        withdrawnDTO.setId(1L);
        withdrawnDTO.setBalance(new BigDecimal("400.00"));
        when(walletService.withdraw(eq(1L), any(BigDecimal.class))).thenReturn(withdrawnDTO);

        mockMvc.perform(post("/api/wallets/1/withdraw?amount=100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(400.00));
    }

    @Test
    void deleteWallet_Returns204() throws Exception {
        doNothing().when(walletService).deleteWallet(1L);

        mockMvc.perform(delete("/api/wallets/1"))
                .andExpect(status().isNoContent());
    }
}
