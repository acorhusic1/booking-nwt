package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.StripeCheckoutResponse;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.model.TransactionType;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.model.WalletTransaction;
import com.bookingnwt.paymentservice.repository.WalletRepository;
import com.bookingnwt.paymentservice.repository.WalletTransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stripe Checkout integracija za wallet top-up.
 *
 * Flow:
 *   1. Frontend zove POST /api/stripe/checkout-session sa amount + walletId
 *   2. createCheckoutSession() pravi Stripe Session, vraca redirect URL
 *   3. Browser redirektuje na Stripe stranicu, korisnik unese test karticu
 *      (4242 4242 4242 4242, bilo koji datum/CVV)
 *   4. Stripe redirektuje nazad na success_url sa ?session_id={CHECKOUT_SESSION_ID}
 *   5. Frontend zove GET /api/stripe/verify-session/{id}
 *   6. verifyAndCreditWallet() provjeri status=paid kod Stripe-a, dosipa wallet,
 *      kreira WalletTransaction
 *
 * Idempotency: vec procesuirane sesije se preskacu (provjera po sessionId
 * u WalletTransaction.description).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Value("${stripe.success-url:http://localhost:3000/dashboard?stripe_session={CHECKOUT_SESSION_ID}}")
    private String successUrl;

    @Value("${stripe.cancel-url:http://localhost:3000/dashboard?stripe_cancelled=1}")
    private String cancelUrl;

    public StripeCheckoutResponse createCheckoutSession(Long walletId, BigDecimal amount) throws StripeException {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet sa ID " + walletId + " nije pronađen"));

        // Stripe radi sa najmanjom jedinicom — za BAM/EUR/USD to su centi (×100)
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("walletId", String.valueOf(walletId))
                .putMetadata("userId", String.valueOf(wallet.getUserId()))
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("bam")
                                .setUnitAmount(amountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("BookingNWT — dosipanje walleta")
                                        .setDescription("Uplata " + amount + " BAM na wallet #" + walletId)
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        log.info("✅ Stripe checkout session kreirana: id={}, wallet={}, amount={}",
                session.getId(), walletId, amount);

        return new StripeCheckoutResponse(session.getId(), session.getUrl());
    }

    /**
     * Verifikuje da je Stripe sesija PLAĆENA i dosipa wallet.
     * Idempotent — ako je sesija već procesuirana, vraca trenutno stanje walleta.
     */
    @Transactional
    public Wallet verifyAndCreditWallet(String sessionId) throws StripeException {
        // Idempotency check — ne dosipaj dva puta za isti session_id
        String txMarker = "Stripe deposit: " + sessionId;
        if (transactionRepository.existsByDescription(txMarker)) {
            log.info("ℹ️ Stripe session {} već procesuirana — preskacem dupli credit", sessionId);
            // Vrati trenutno stanje walleta (treba ga ipak naći iz metadata)
            Session session = Session.retrieve(sessionId);
            Long walletId = Long.valueOf(session.getMetadata().get("walletId"));
            return walletRepository.findById(walletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet nije pronađen"));
        }

        Session session = Session.retrieve(sessionId);

        if (!"paid".equals(session.getPaymentStatus())) {
            throw new IllegalStateException("Stripe sesija nije plaćena (status=" + session.getPaymentStatus() + ")");
        }

        Long walletId = Long.valueOf(session.getMetadata().get("walletId"));
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet sa ID " + walletId + " nije pronađen"));

        // amount_total je u centima, treba pretvoriti nazad
        BigDecimal amount = BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100));

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction(wallet, amount, TransactionType.DEPOSIT, txMarker, null);
        transactionRepository.save(tx);

        log.info("💳 Stripe deposit uspješan: wallet={}, amount={} BAM, novi balance={}",
                walletId, amount, wallet.getBalance());

        return wallet;
    }
}
