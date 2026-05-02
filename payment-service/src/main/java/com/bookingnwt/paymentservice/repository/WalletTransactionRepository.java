package com.bookingnwt.paymentservice.repository;

import com.bookingnwt.paymentservice.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletId(Long walletId);
    List<WalletTransaction> findByPaymentId(Long paymentId);
}
