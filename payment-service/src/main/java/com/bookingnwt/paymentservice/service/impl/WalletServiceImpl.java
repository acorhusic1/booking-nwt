package com.bookingnwt.paymentservice.service.impl;

import com.bookingnwt.paymentservice.dto.WalletRequestDTO;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.WalletMapper;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.model.WalletTransaction;
import com.bookingnwt.paymentservice.model.TransactionType;
import com.bookingnwt.paymentservice.repository.WalletRepository;
import com.bookingnwt.paymentservice.repository.WalletTransactionRepository;
import com.bookingnwt.paymentservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    @Override
    public WalletResponseDTO createWallet(WalletRequestDTO dto) {
        // Idempotent — ako user vec ima wallet, vrati postojeci.
        // Bez ovog check-a, Dashboard auto-create i manualni Create wallet
        // mogu napraviti DUPLIKATE; findByUserId vraca jedan random,
        // payment-service skida pare sa pogresnog wallet-a.
        return walletRepository.findByUserId(dto.getUserId())
                .map(walletMapper::toDTO)
                .orElseGet(() -> {
                    Wallet wallet = walletMapper.toEntity(dto);
                    return walletMapper.toDTO(walletRepository.save(wallet));
                });
    }

    @Override
    public WalletResponseDTO getWalletById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novčanik sa ID " + id + " nije pronađen"));
        return walletMapper.toDTO(wallet);
    }

    @Override
    public WalletResponseDTO getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Novčanik za korisnika " + userId + " nije pronađen"));
        return walletMapper.toDTO(wallet);
    }

    @Override
    public List<WalletResponseDTO> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(walletMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WalletResponseDTO deposit(Long walletId, BigDecimal amount) {
        // BUG-007: validacija da amount nije null/negativan/preveliki
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Iznos uplate mora biti pozitivan");
        }
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalArgumentException("Iznos uplate ne smije prelaziti 100,000");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Novčanik sa ID " + walletId + " nije pronađen"));
        wallet.setBalance(wallet.getBalance().add(amount));

        WalletTransaction tx = new WalletTransaction(wallet, amount, TransactionType.DEPOSIT,
                "Uplata na novčanik", null);
        transactionRepository.save(tx);

        return walletMapper.toDTO(walletRepository.save(wallet));
    }

    @Override
    public WalletResponseDTO withdraw(Long walletId, BigDecimal amount) {
        // BUG-007: validacija
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Iznos isplate mora biti pozitivan");
        }
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalArgumentException("Iznos isplate ne smije prelaziti 100,000");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Novčanik sa ID " + walletId + " nije pronađen"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Nedovoljno sredstava na novčaniku");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        WalletTransaction tx = new WalletTransaction(wallet, amount.negate(), TransactionType.WITHDRAWAL,
                "Isplata sa novčanika", null);
        transactionRepository.save(tx);

        return walletMapper.toDTO(walletRepository.save(wallet));
    }

    @Override
    public void deleteWallet(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new ResourceNotFoundException("Novčanik sa ID " + id + " nije pronađen");
        }
        walletRepository.deleteById(id);
    }
}
