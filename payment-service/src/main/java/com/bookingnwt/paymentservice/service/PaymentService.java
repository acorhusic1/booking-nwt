package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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

    // Task 4 — non-trivial endpoints
    PaymentResponseDTO patchPayment(Long id, JsonNode patch);
    Page<PaymentResponseDTO> getPaymentsByGuestPaged(Long guestId, Pageable pageable);
    BigDecimal getTotalSpentByGuest(Long guestId);
    List<PaymentResponseDTO> findByStatusAndMinAmount(PaymentStatus status, BigDecimal minAmount);
    long countByStatus(PaymentStatus status);
    List<PaymentResponseDTO> batchCreate(List<PaymentRequestDTO> dtos);
    PaymentResponseDTO getPaymentWithDetails(Long id);
}
