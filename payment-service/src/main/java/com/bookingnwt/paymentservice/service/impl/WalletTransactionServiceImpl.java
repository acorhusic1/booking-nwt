package com.bookingnwt.paymentservice.service.impl;

import com.bookingnwt.paymentservice.dto.WalletTransactionResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.WalletTransactionMapper;
import com.bookingnwt.paymentservice.repository.WalletTransactionRepository;
import com.bookingnwt.paymentservice.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository transactionRepository;
    private final WalletTransactionMapper transactionMapper;

    @Override
    public WalletTransactionResponseDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transakcija sa ID " + id + " nije pronađena"));
    }

    @Override
    public List<WalletTransactionResponseDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletTransactionResponseDTO> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId).stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletTransactionResponseDTO> getTransactionsByPaymentId(Long paymentId) {
        return transactionRepository.findByPaymentId(paymentId).stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
