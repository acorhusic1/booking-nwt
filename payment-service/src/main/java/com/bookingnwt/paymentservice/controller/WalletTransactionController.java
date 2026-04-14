package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.WalletTransactionResponseDTO;
import com.bookingnwt.paymentservice.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class WalletTransactionController {

    private final WalletTransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<WalletTransactionResponseDTO> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping
    public ResponseEntity<List<WalletTransactionResponseDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<WalletTransactionResponseDTO>> getTransactionsByWalletId(@PathVariable Long walletId) {
        return ResponseEntity.ok(transactionService.getTransactionsByWalletId(walletId));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<WalletTransactionResponseDTO>> getTransactionsByPaymentId(@PathVariable Long paymentId) {
        return ResponseEntity.ok(transactionService.getTransactionsByPaymentId(paymentId));
    }
}
