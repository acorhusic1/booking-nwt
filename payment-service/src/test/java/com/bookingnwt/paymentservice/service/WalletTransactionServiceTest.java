package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.WalletTransactionResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.WalletTransactionMapper;
import com.bookingnwt.paymentservice.model.TransactionType;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.model.WalletTransaction;
import com.bookingnwt.paymentservice.repository.WalletTransactionRepository;
import com.bookingnwt.paymentservice.service.impl.WalletTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletTransactionServiceTest {

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private WalletTransactionMapper transactionMapper;

    @InjectMocks
    private WalletTransactionServiceImpl transactionService;

    private WalletTransaction transaction;
    private WalletTransactionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet(10L, new BigDecimal("500.00"), "BAM");
        wallet.setId(1L);

        transaction = new WalletTransaction(wallet, new BigDecimal("100.00"),
                TransactionType.DEPOSIT, "Test deposit", null);
        transaction.setId(1L);
        transaction.setCreatedAt(LocalDateTime.now());

        responseDTO = new WalletTransactionResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setWalletId(1L);
        responseDTO.setAmount(new BigDecimal("100.00"));
        responseDTO.setType("DEPOSIT");
        responseDTO.setDescription("Test deposit");
    }

    @Test
    void getTransactionById_Found() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(responseDTO);

        WalletTransactionResponseDTO result = transactionService.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DEPOSIT", result.getType());
    }

    @Test
    void getTransactionById_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(99L));
    }

    @Test
    void getAllTransactions_ReturnsList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(responseDTO);

        List<WalletTransactionResponseDTO> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByWalletId_ReturnsList() {
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(responseDTO);

        List<WalletTransactionResponseDTO> result = transactionService.getTransactionsByWalletId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByPaymentId_ReturnsList() {
        when(transactionRepository.findByPaymentId(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(responseDTO);

        List<WalletTransactionResponseDTO> result = transactionService.getTransactionsByPaymentId(1L);

        assertEquals(1, result.size());
    }
}
