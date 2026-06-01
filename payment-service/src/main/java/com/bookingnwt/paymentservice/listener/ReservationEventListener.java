package com.bookingnwt.paymentservice.listener;

import com.bookingnwt.paymentservice.events.PaymentCompletedEvent;
import com.bookingnwt.paymentservice.events.PaymentFailedEvent;
import com.bookingnwt.paymentservice.events.ReservationCreatedEvent;
import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.model.Wallet;
import com.bookingnwt.paymentservice.publisher.PaymentEventPublisher;
import com.bookingnwt.paymentservice.repository.PaymentRepository;
import com.bookingnwt.paymentservice.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SAGA PATTERN — Payment Service Listener (Task 3).
 *
 * KORAK 2 saga-e: prima ReservationCreatedEvent, pokušava naplatiti
 * iznos sa wallet-a gosta i emituje rezultat:
 *
 *   walletBalance >= totalPrice  →  PaymentCompletedEvent  (FINAL)
 *   wallet ne postoji ili nedovoljno → PaymentFailedEvent  (KOMPENZACIJA)
 *
 * Lokalna transakcija je @Transactional — ili upišemo i Payment row i
 * promijenimo wallet balance, ili ništa od to dvoje.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.payment}")
    public void onMessage(String message) {
        try {
            ReservationCreatedEvent event = objectMapper.readValue(message, ReservationCreatedEvent.class);
            log.info("📨 Primljen ReservationCreatedEvent za rezervaciju {} (iznos {} {})",
                    event.getReservationId(), event.getTotalPrice(), event.getCurrency());
            processPayment(event);
        } catch (Exception e) {
            log.error("❌ Greška pri parsiranju ReservationCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Lokalna transakcija — atomarno: insert Payment + smanji balance walleta.
     * Ako wallet ne postoji ili nema dovoljno → FAILED + emit PaymentFailedEvent.
     * Ako prođe → COMPLETED + emit PaymentCompletedEvent.
     */
    @Transactional
    public void processPayment(ReservationCreatedEvent event) {
        BigDecimal amount = event.getTotalPrice() != null ? event.getTotalPrice() : BigDecimal.ZERO;
        String currency = event.getCurrency() != null ? event.getCurrency() : "BAM";

        Optional<Wallet> walletOpt = walletRepository.findByUserId(event.getUserId());

        // FAILED grana — wallet ne postoji
        if (walletOpt.isEmpty()) {
            Payment failed = persistPayment(event, amount, currency, PaymentStatus.FAILED);
            paymentEventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    failed.getId(), event.getReservationId(), event.getPropertyId(),
                    event.getUserId(),
                    "Wallet ne postoji za korisnika " + event.getUserId(),
                    LocalDateTime.now(), "PAYMENT_FAILED",
                    event.getHostId()
            ));
            return;
        }

        Wallet wallet = walletOpt.get();

        // FAILED grana — nedovoljno sredstava
        if (wallet.getBalance().compareTo(amount) < 0) {
            Payment failed = persistPayment(event, amount, currency, PaymentStatus.FAILED);
            paymentEventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    failed.getId(), event.getReservationId(), event.getPropertyId(),
                    event.getUserId(),
                    "Nedovoljno sredstava (balance=" + wallet.getBalance() + ", potrebno=" + amount + ")",
                    LocalDateTime.now(), "PAYMENT_FAILED",
                    event.getHostId()
            ));
            return;
        }

        // SUCCESS grana — skini iznos s walleta i upiši COMPLETED Payment
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Payment completed = persistPayment(event, amount, currency, PaymentStatus.COMPLETED);
        paymentEventPublisher.publishPaymentCompleted(new PaymentCompletedEvent(
                completed.getId(), event.getReservationId(), event.getUserId(),
                amount, currency, LocalDateTime.now(), "PAYMENT_COMPLETED"
        ));

        // F19 — host payout sa komisijom platforme.
        // Spec: "Domacin prima isplatu na svoj racun nakon uspjesno zavrsenog
        // boravka gosta, umanjenu za proviziju platforme." Za demo: kreditiramo
        // host wallet odmah (komisija = 10%). U produkciji bi cekali COMPLETED status.
        if (event.getHostId() != null) {
            BigDecimal commissionPct = BigDecimal.valueOf(10);
            BigDecimal commission = amount.multiply(commissionPct)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal hostPayout = amount.subtract(commission);

            Wallet hostWallet = walletRepository.findByUserId(event.getHostId())
                    .orElseGet(() -> {
                        Wallet w = new Wallet(event.getHostId(), BigDecimal.ZERO, currency);
                        return walletRepository.save(w);
                    });
            hostWallet.setBalance(hostWallet.getBalance().add(hostPayout));
            hostWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(hostWallet);
            log.info("💵 Host {} payout: {} {} ({} odvojeno za platformu kao komisija 10%)",
                    event.getHostId(), hostPayout, currency, commission);
        }
    }

    private Payment persistPayment(ReservationCreatedEvent event,
                                   BigDecimal amount, String currency, PaymentStatus status) {
        Payment payment = new Payment(event.getReservationId(), event.getUserId(),
                amount, currency, "WALLET");
        payment.setStatus(status);
        payment.setProcessedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}
