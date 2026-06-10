package com.bookingnwt.paymentservice.listener;

import com.bookingnwt.paymentservice.events.ReservationCancelledEvent;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SAGA KOMPENZACIJA — kad guest otkaže rezervaciju, payment-service vraća
 * pare na wallet i markira Payment kao REFUNDED.
 *
 * Slušamo na **istom** payment.service.queue-u koji već prima
 * ReservationCreatedEvent — samo dodajemo novi binding za routing key
 * `booking.reservation.cancelled` (vidi RabbitMQConfig).
 *
 * Idempotent: ako je Payment već REFUNDED (npr. dupli event), ne refundiramo
 * ponovo. Ako payment nije COMPLETED (npr. ostao FAILED iz početne saga
 * kompenzacije), nema šta refundirati.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCancelledListener {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.payment-cancellations}")
    @Transactional
    public void onMessage(String message) {
        try {
            ReservationCancelledEvent event = objectMapper.readValue(message, ReservationCancelledEvent.class);
            log.info("📨 ReservationCancelledEvent primljen za rezervaciju {}", event.getReservationId());

            List<Payment> payments = paymentRepository.findByReservationId(event.getReservationId());
            Optional<Payment> toRefund = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                    .findFirst();

            if (toRefund.isEmpty()) {
                log.info("ℹ️ Nema COMPLETED payment-a za rezervaciju {} — nema šta refundirati " +
                        "(payment je vjerovatno bio FAILED ili je već REFUNDED)", event.getReservationId());
                return;
            }

            Payment payment = toRefund.get();

            // F6 — refund po procentu iz cancellation policy (default 100% ako stari publisher).
            int refundPct = event.getRefundPercentage() != null ? event.getRefundPercentage() : 100;
            java.math.BigDecimal refundAmount = payment.getAmount()
                    .multiply(java.math.BigDecimal.valueOf(refundPct))
                    .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // Vrati iznos na wallet
            Optional<Wallet> walletOpt = walletRepository.findByUserId(payment.getGuestId());
            if (walletOpt.isPresent() && refundAmount.signum() > 0) {
                Wallet wallet = walletOpt.get();
                wallet.setBalance(wallet.getBalance().add(refundAmount));
                wallet.setUpdatedAt(LocalDateTime.now());
                walletRepository.save(wallet);
                // F19 — refund vidljiv u historiji novcanika
                walletTransactionRepository.save(new WalletTransaction(
                        wallet, refundAmount, TransactionType.REFUND,
                        "Povrat (" + refundPct + "%) za rezervaciju #" + event.getReservationId(), payment));
                log.info("💰 Refund {} {} ({}% od {}) na wallet korisnika {} (novi balance: {})",
                        refundAmount, payment.getCurrency(), refundPct, payment.getAmount(),
                        payment.getGuestId(), wallet.getBalance());
            } else if (refundAmount.signum() == 0) {
                log.info("ℹ️ Refund {}% = 0 BAM (politika bez povrata) za payment {}",
                        refundPct, payment.getId());
            } else {
                log.warn("⚠️ Wallet ne postoji za korisnika {} — refund preskočen", payment.getGuestId());
            }

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log.info("✅ Payment {} markiran kao REFUNDED", payment.getId());

        } catch (Exception e) {
            log.error("❌ Greška u ReservationCancelledListener: {}", e.getMessage(), e);
        }
    }
}
