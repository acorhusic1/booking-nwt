package com.bookingnwt.paymentservice.config;

import com.bookingnwt.paymentservice.model.*;
import com.bookingnwt.paymentservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initPaymentData(WalletRepository walletRepo,
                                       PaymentRepository paymentRepo,
                                       WalletTransactionRepository transactionRepo) {
        return args -> {
            // Idempotent — preskoci ako podaci vec postoje (ddl-auto=update zadrzava)
            if (walletRepo.count() > 0) {
                System.out.println("=== Payment Service: DB vec ima podatke, preskacem seed ===");
                return;
            }

            // --- Virtualni novčanici ---
            walletRepo.save(new Wallet(2L, new BigDecimal("1500.00"), "BAM"));
            Wallet w2 = walletRepo.save(new Wallet(3L, new BigDecimal("800.00"), "BAM"));
            Wallet w3 = walletRepo.save(new Wallet(4L, new BigDecimal("500.00"), "BAM"));
            Wallet w4 = walletRepo.save(new Wallet(5L, new BigDecimal("250.00"), "BAM"));
            Wallet w5 = walletRepo.save(new Wallet(6L, new BigDecimal("300.00"), "BAM"));

            // --- Uplate na novčanike (deposit) ---
            transactionRepo.save(new WalletTransaction(w3, new BigDecimal("500.00"),
                    TransactionType.DEPOSIT, "Inicijalni deposit", null));
            transactionRepo.save(new WalletTransaction(w4, new BigDecimal("250.00"),
                    TransactionType.DEPOSIT, "Inicijalni deposit", null));
            transactionRepo.save(new WalletTransaction(w5, new BigDecimal("300.00"),
                    TransactionType.DEPOSIT, "Inicijalni deposit", null));

            // --- Plaćanje za rezervaciju 1 ---
            Payment pay1 = new Payment(1L, 4L, new BigDecimal("375.00"), "BAM", "WALLET");
            pay1.setStatus(PaymentStatus.COMPLETED);
            pay1.setProcessedAt(LocalDateTime.now().minusDays(10));
            paymentRepo.save(pay1);

            transactionRepo.save(new WalletTransaction(w3, new BigDecimal("-375.00"),
                    TransactionType.PAYMENT, "Plaćanje rezervacije #1", pay1));

            // --- Plaćanje za rezervaciju 2 ---
            Payment pay2 = new Payment(2L, 5L, new BigDecimal("1050.00"), "BAM", "CREDIT_CARD");
            pay2.setStatus(PaymentStatus.COMPLETED);
            pay2.setProcessedAt(LocalDateTime.now().minusDays(5));
            paymentRepo.save(pay2);

            // --- Plaćanje za rezervaciju 3 + povrat ---
            Payment pay3 = new Payment(3L, 6L, new BigDecimal("100.00"), "BAM", "WALLET");
            pay3.setStatus(PaymentStatus.COMPLETED);
            pay3.setProcessedAt(LocalDateTime.now().minusDays(30));
            paymentRepo.save(pay3);

            transactionRepo.save(new WalletTransaction(w5, new BigDecimal("-100.00"),
                    TransactionType.PAYMENT, "Plaćanje rezervacije #3", pay3));

            // --- Isplata domaćinu za završenu rezervaciju 3 ---
            Payment payout1 = new Payment(3L, 3L, new BigDecimal("90.00"), "BAM", "BANK_TRANSFER");
            payout1.setStatus(PaymentStatus.COMPLETED);
            payout1.setProcessedAt(LocalDateTime.now().minusDays(25));
            paymentRepo.save(payout1);

            transactionRepo.save(new WalletTransaction(w2, new BigDecimal("90.00"),
                    TransactionType.PAYOUT, "Isplata za rezervaciju #3 (umanjeno za proviziju)", payout1));

            System.out.println("=== Payment Service: Učitano " + walletRepo.count() + " novčanika ===");
            System.out.println("=== Payment Service: Učitano " + paymentRepo.count() + " plaćanja ===");
            System.out.println("=== Payment Service: Učitano " + transactionRepo.count() + " transakcija ===");
        };
    }
}
