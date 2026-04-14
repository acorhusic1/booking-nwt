package com.bookingnwt.paymentservice.repository;

import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByReservationId(Long reservationId);
    List<Payment> findByGuestId(Long guestId);
    List<Payment> findByStatus(PaymentStatus status);
}
