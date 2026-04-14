package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.WalletRequestDTO;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.WalletMapper;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.model.WalletTransaction;
import com.bookingnwt.paymentservice.repository.WalletRepository;
import com.bookingnwt.paymentservice.repository.WalletTransactionRepository;
import com.bookingnwt.paymentservice.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet wallet;
    private WalletRequestDTO requestDTO;
    private WalletResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        wallet = new Wallet(10L, new BigDecimal("500.00"), "BAM");
        wallet.setId(1L);

        requestDTO = new WalletRequestDTO();
        requestDTO.setUserId(10L);
        requestDTO.setBalance(new BigDecimal("500.00"));
        requestDTO.setCurrency("BAM");

        responseDTO = new WalletResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(10L);
        responseDTO.setBalance(new BigDecimal("500.00"));
        responseDTO.setCurrency("BAM");
    }

    @Test
    void createWallet_ReturnsDTO() {
        when(walletMapper.toEntity(requestDTO)).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletMapper.toDTO(wallet)).thenReturn(responseDTO);

        WalletResponseDTO result = walletService.createWallet(requestDTO);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void getWalletById_Found() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletMapper.toDTO(wallet)).thenReturn(responseDTO);

        WalletResponseDTO result = walletService.getWalletById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getWalletById_NotFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> walletService.getWalletById(99L));
    }

    @Test
    void getWalletByUserId_Found() {
        when(walletRepository.findByUserId(10L)).thenReturn(Optional.of(wallet));
        when(walletMapper.toDTO(wallet)).thenReturn(responseDTO);

        WalletResponseDTO result = walletService.getWalletByUserId(10L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
    }

    @Test
    void getWalletByUserId_NotFound() {
        when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> walletService.getWalletByUserId(99L));
    }

    @Test
    void getAllWallets_ReturnsList() {
        when(walletRepository.findAll()).thenReturn(List.of(wallet));
        when(walletMapper.toDTO(wallet)).thenReturn(responseDTO);

        List<WalletResponseDTO> result = walletService.getAllWallets();

        assertEquals(1, result.size());
    }

    @Test
    void deposit_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(null);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletResponseDTO depositedDTO = new WalletResponseDTO();
        depositedDTO.setId(1L);
        depositedDTO.setBalance(new BigDecimal("600.00"));
        when(walletMapper.toDTO(wallet)).thenReturn(depositedDTO);

        WalletResponseDTO result = walletService.deposit(1L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("600.00"), result.getBalance());
    }

    @Test
    void withdraw_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(null);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletResponseDTO withdrawnDTO = new WalletResponseDTO();
        withdrawnDTO.setId(1L);
        withdrawnDTO.setBalance(new BigDecimal("400.00"));
        when(walletMapper.toDTO(wallet)).thenReturn(withdrawnDTO);

        WalletResponseDTO result = walletService.withdraw(1L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("400.00"), result.getBalance());
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.withdraw(1L, new BigDecimal("999.00")));
    }

    @Test
    void deleteWallet_Success() {
        when(walletRepository.existsById(1L)).thenReturn(true);
        doNothing().when(walletRepository).deleteById(1L);

        assertDoesNotThrow(() -> walletService.deleteWallet(1L));
        verify(walletRepository).deleteById(1L);
    }

    @Test
    void deleteWallet_NotFound() {
        when(walletRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> walletService.deleteWallet(99L));
    }
}
