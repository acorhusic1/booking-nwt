package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.WalletRequestDTO;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;
import com.bookingnwt.paymentservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<WalletResponseDTO> createWallet(@Valid @RequestBody WalletRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<WalletResponseDTO> getWalletById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WalletResponseDTO>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWallets());
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<WalletResponseDTO> deposit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.deposit(id, amount));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN')")
    public ResponseEntity<WalletResponseDTO> withdraw(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.withdraw(id, amount));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}
