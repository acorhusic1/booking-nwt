package com.bookingnwt.paymentservice.service.impl;

import com.bookingnwt.paymentservice.client.ReservationStatusGateway;
import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.PaymentMapper;
import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.repository.PaymentRepository;
import com.bookingnwt.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;
    private final ReservationStatusGateway reservationStatusGateway;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setStatus(PaymentStatus.PENDING);
        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));
        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByGuestId(Long guestId) {
        return paymentRepository.findByGuestId(guestId).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));
        payment.setStatus(status);
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED) {
            payment.setProcessedAt(LocalDateTime.now());
        }
        Payment saved = paymentRepository.save(payment);

        // Task 5 — synchronously notify reservation-service when payment lifecycle changes
        if (status == PaymentStatus.COMPLETED) {
            reservationStatusGateway.updateStatus(saved.getReservationId(), "CONFIRMED");
        } else if (status == PaymentStatus.FAILED) {
            reservationStatusGateway.updateStatus(saved.getReservationId(), "CANCELLED");
        }

        return paymentMapper.toDTO(saved);
    }

    @Override
    @Transactional
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
        Payment saved = paymentRepository.save(refund);

        // Task 5 — refund flips reservation back to CANCELLED
        reservationStatusGateway.updateStatus(saved.getReservationId(), "CANCELLED");

        return paymentMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno");
        }
        paymentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PaymentResponseDTO patchPayment(Long id, JsonNode patchNode) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));

        PaymentResponseDTO current = paymentMapper.toDTO(existing);
        try {
            JsonPatch patch = JsonPatch.fromJson(patchNode);
            JsonNode patched = patch.apply(objectMapper.convertValue(current, JsonNode.class));
            PaymentResponseDTO updated = objectMapper.treeToValue(patched, PaymentResponseDTO.class);

            existing.setAmount(updated.getAmount());
            existing.setCurrency(updated.getCurrency());
            existing.setMethod(updated.getMethod());
            if (updated.getStatus() != null) {
                existing.setStatus(PaymentStatus.valueOf(updated.getStatus()));
            }
            existing.setProcessedAt(updated.getProcessedAt());

            return paymentMapper.toDTO(paymentRepository.save(existing));
        } catch (JsonPatchException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Neispravna JSON Patch operacija: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Neuspjesno parsiranje patch-a: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByGuestPaged(Long guestId, Pageable pageable) {
        return paymentRepository.findByGuestId(guestId, pageable).map(paymentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpentByGuest(Long guestId) {
        BigDecimal sum = paymentRepository.totalSpentByGuest(guestId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findByStatusAndMinAmount(PaymentStatus status, BigDecimal minAmount) {
        if (minAmount == null || minAmount.signum() < 0) {
            throw new IllegalArgumentException("Minimalni iznos mora biti >= 0");
        }
        return paymentRepository.findByStatusAndMinAmount(status, minAmount).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }

    @Override
    @Transactional
    public List<PaymentResponseDTO> batchCreate(List<PaymentRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("Lista plaćanja ne smije biti prazna");
        }
        List<Payment> entities = dtos.stream().map(dto -> {
            Payment p = paymentMapper.toEntity(dto);
            p.setStatus(PaymentStatus.PENDING);
            return p;
        }).collect(Collectors.toList());

        return paymentRepository.saveAll(entities).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentWithDetails(Long id) {
        Payment payment = paymentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaćanje sa ID " + id + " nije pronađeno"));
        return paymentMapper.toDTO(payment);
    }
}
