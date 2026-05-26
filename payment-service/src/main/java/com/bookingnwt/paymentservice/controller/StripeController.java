package com.bookingnwt.paymentservice.controller;

import com.bookingnwt.paymentservice.dto.StripeCheckoutRequest;
import com.bookingnwt.paymentservice.dto.StripeCheckoutResponse;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;
import com.bookingnwt.paymentservice.mapper.WalletMapper;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.service.StripeService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoint-i za Stripe Checkout integraciju.
 *
 *   POST /api/stripe/checkout-session  → kreira sesiju, vraća URL za redirect
 *   GET  /api/stripe/verify-session/{id} → verifikuje placanje, dosipa wallet
 */
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;
    private final WalletMapper walletMapper;

    @PostMapping("/checkout-session")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST', 'ADMIN')")
    public ResponseEntity<StripeCheckoutResponse> createCheckoutSession(
            @Valid @RequestBody StripeCheckoutRequest request) {
        try {
            StripeCheckoutResponse response = stripeService.createCheckoutSession(
                    request.getWalletId(), request.getAmount());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("❌ Stripe checkout-session greska: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new StripeCheckoutResponse(null, null));
        }
    }

    @GetMapping("/verify-session/{sessionId}")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST', 'ADMIN')")
    public ResponseEntity<?> verifySession(@PathVariable String sessionId) {
        try {
            Wallet wallet = stripeService.verifyAndCreditWallet(sessionId);
            WalletResponseDTO dto = walletMapper.toDTO(wallet);
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            log.error("❌ Stripe verify-session greska: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Stripe API greška: " + e.getMessage()));
        }
    }
}
