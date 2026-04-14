package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.WalletRequestDTO;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletResponseDTO createWallet(WalletRequestDTO dto);
    WalletResponseDTO getWalletById(Long id);
    WalletResponseDTO getWalletByUserId(Long userId);
    List<WalletResponseDTO> getAllWallets();
    WalletResponseDTO deposit(Long walletId, BigDecimal amount);
    WalletResponseDTO withdraw(Long walletId, BigDecimal amount);
    void deleteWallet(Long id);
}
