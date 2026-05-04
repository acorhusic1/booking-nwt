package com.bookingnwt.paymentservice.repository;

import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByReservationId(Long reservationId);
    List<Payment> findByGuestId(Long guestId);
    List<Payment> findByStatus(PaymentStatus status);

    @EntityGraph(attributePaths = {"relatedPayment", "walletTransactions"})
    Optional<Payment> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"relatedPayment"})
    Page<Payment> findByGuestId(Long guestId, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.guestId = :guestId
              AND p.status = com.bookingnwt.paymentservice.model.PaymentStatus.COMPLETED
            """)
    BigDecimal totalSpentByGuest(@Param("guestId") Long guestId);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.status = :status
              AND p.amount >= :minAmount
            ORDER BY p.amount DESC
            """)
    List<Payment> findByStatusAndMinAmount(@Param("status") PaymentStatus status,
                                           @Param("minAmount") BigDecimal minAmount);

    long countByStatus(PaymentStatus status);
}
