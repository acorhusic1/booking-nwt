package com.bookingnwt.paymentservice.listener;

import com.bookingnwt.paymentservice.events.ReservationCompletedEvent;
import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.model.TransactionType;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.model.WalletTransaction;
import com.bookingnwt.paymentservice.repository.PaymentRepository;
import com.bookingnwt.paymentservice.repository.WalletRepository;
import com.bookingnwt.paymentservice.repository.WalletTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * F19 — isplata domaćinu NAKON završenog boravka.
 *
 * reservation-service scheduler prebaci rezervaciju u COMPLETED i emituje
 * ReservationCompletedEvent. Ovdje:
 *   1. nađemo COMPLETED payment za tu rezervaciju (ako je REFUNDED ili FAILED,
 *      nema isplate — gost je dobio pare nazad ili nikad nije ni platio)
 *   2. idempotency: ako PAYOUT transakcija za taj payment već postoji,
 *      preskačemo (dupli event ne smije duplo isplatiti)
 *   3. kreditiramo host wallet sa iznosom umanjenim za proviziju platforme (10%)
 *      i upišemo PAYOUT u historiju novčanika
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCompletedListener {

    private static final BigDecimal COMMISSION_PCT = BigDecimal.valueOf(10);

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.payment-completions:payment.completions.queue}")
    @Transactional
    public void onMessage(String message) {
        try {
            ReservationCompletedEvent event = objectMapper.readValue(message, ReservationCompletedEvent.class);
            log.info("📨 ReservationCompletedEvent primljen za rezervaciju {} (host={})",
                    event.getReservationId(), event.getHostId());

            if (event.getHostId() == null) {
                log.warn("⚠️ Event bez hostId — payout preskočen (rezervacija {})", event.getReservationId());
                return;
            }

            List<Payment> payments = paymentRepository.findByReservationId(event.getReservationId());
            Optional<Payment> completedPayment = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                    .findFirst();

            if (completedPayment.isEmpty()) {
                log.info("ℹ️ Nema COMPLETED payment-a za rezervaciju {} — payout preskočen " +
                        "(payment je FAILED ili REFUNDED)", event.getReservationId());
                return;
            }

            Payment payment = completedPayment.get();

            // Idempotency — jedan PAYOUT po payment-u, bez obzira na duple evente
            if (walletTransactionRepository.existsByPaymentIdAndType(payment.getId(), TransactionType.PAYOUT)) {
                log.info("ℹ️ Payout za payment {} već postoji — preskačem (idempotent)", payment.getId());
                return;
            }

            BigDecimal amount = payment.getAmount();
            BigDecimal commission = amount.multiply(COMMISSION_PCT)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal hostPayout = amount.subtract(commission);
            String currency = payment.getCurrency() != null ? payment.getCurrency() : "BAM";

            Wallet hostWallet = walletRepository.findByUserId(event.getHostId())
                    .orElseGet(() -> walletRepository.save(
                            new Wallet(event.getHostId(), BigDecimal.ZERO, currency)));
            hostWallet.setBalance(hostWallet.getBalance().add(hostPayout));
            hostWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(hostWallet);

            walletTransactionRepository.save(new WalletTransaction(
                    hostWallet, hostPayout, TransactionType.PAYOUT,
                    "Isplata za rezervaciju #" + event.getReservationId() + " (provizija 10%)", payment));

            log.info("💵 Host {} payout: {} {} (provizija platforme: {})",
                    event.getHostId(), hostPayout, currency, commission);

        } catch (Exception e) {
            log.error("❌ Greška u ReservationCompletedListener: {}", e.getMessage(), e);
        }
    }
}
