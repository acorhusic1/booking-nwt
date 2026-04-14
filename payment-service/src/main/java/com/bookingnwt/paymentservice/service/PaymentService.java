package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.model.PaymentStatus;

import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO dto);
    PaymentResponseDTO getPaymentById(Long id);
    List<PaymentResponseDTO> getAllPayments();
    List<PaymentResponseDTO> getPaymentsByReservationId(Long reservationId);
    List<PaymentResponseDTO> getPaymentsByGuestId(Long guestId);
    List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status);
    PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatus status);
    PaymentResponseDTO refundPayment(Long id);
    void deletePayment(Long id);
}
