package com.bookingnwt.paymentservice.service.impl;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.PaymentMapper;
import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.repository.PaymentRepository;
import com.bookingnwt.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setStatus(PaymentStatus.PENDING);
        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByGuestId(Long guestId) {
        return paymentRepository.findByGuestId(guestId).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));
        payment.setStatus(status);
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED) {
            payment.setProcessedAt(LocalDateTime.now());
        }
        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDTO refundPayment(Long id) {
        Payment original = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));

        if (original.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Samo završena plaćanja mogu biti refundirana");
        }

        original.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(original);

        Payment refund = new Payment(original.getReservationId(), original.getGuestId(),
                original.getAmount(), original.getCurrency(), original.getMethod());
        refund.setStatus(PaymentStatus.REFUNDED);
        refund.setProcessedAt(LocalDateTime.now());
        refund.setRelatedPayment(original);

        return paymentMapper.toDTO(paymentRepository.save(refund));
    }

    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno");
        }
        paymentRepository.deleteById(id);
    }
}
