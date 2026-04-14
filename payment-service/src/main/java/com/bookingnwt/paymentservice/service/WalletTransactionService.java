package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.WalletTransactionResponseDTO;

import java.util.List;

public interface WalletTransactionService {
    WalletTransactionResponseDTO getTransactionById(Long id);
    List<WalletTransactionResponseDTO> getAllTransactions();
    List<WalletTransactionResponseDTO> getTransactionsByWalletId(Long walletId);
    List<WalletTransactionResponseDTO> getTransactionsByPaymentId(Long paymentId);
}
