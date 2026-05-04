package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.PaymentMapper;
import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.client.ReservationStatusGateway;
import com.bookingnwt.paymentservice.repository.PaymentRepository;
import com.bookingnwt.paymentservice.service.impl.PaymentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReservationStatusGateway reservationStatusGateway;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentRequestDTO requestDTO;
    private PaymentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        payment = new Payment(1L, 4L, new BigDecimal("200.00"), "BAM", "WALLET");
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);

        requestDTO = new PaymentRequestDTO();
        requestDTO.setReservationId(1L);
        requestDTO.setGuestId(4L);
        requestDTO.setAmount(new BigDecimal("200.00"));
        requestDTO.setCurrency("BAM");
        requestDTO.setMethod("WALLET");

        responseDTO = new PaymentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setReservationId(1L);
        responseDTO.setGuestId(4L);
        responseDTO.setAmount(new BigDecimal("200.00"));
        responseDTO.setCurrency("BAM");
        responseDTO.setStatus("PENDING");
        responseDTO.setMethod("WALLET");
    }

    @Test
    void createPayment_ReturnsDTO() {
        when(paymentMapper.toEntity(requestDTO)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDTO(payment)).thenReturn(responseDTO);

        PaymentResponseDTO result = paymentService.createPayment(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getReservationId());
        assertEquals("PENDING", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentById_Found() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(responseDTO);

        PaymentResponseDTO result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPaymentById_NotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(99L));
    }

    @Test
    void getAllPayments_ReturnsList() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(responseDTO);

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
    }

    @Test
    void getPaymentsByReservationId_ReturnsList() {
        when(paymentRepository.findByReservationId(1L)).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(responseDTO);

        List<PaymentResponseDTO> result = paymentService.getPaymentsByReservationId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getPaymentsByGuestId_ReturnsList() {
        when(paymentRepository.findByGuestId(4L)).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(responseDTO);

        List<PaymentResponseDTO> result = paymentService.getPaymentsByGuestId(4L);

        assertEquals(1, result.size());
    }

    @Test
    void getPaymentsByStatus_ReturnsList() {
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(responseDTO);

        List<PaymentResponseDTO> result = paymentService.getPaymentsByStatus(PaymentStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void updatePaymentStatus_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDTO completedDTO = new PaymentResponseDTO();
        completedDTO.setId(1L);
        completedDTO.setStatus("COMPLETED");
        when(paymentMapper.toDTO(payment)).thenReturn(completedDTO);

        PaymentResponseDTO result = paymentService.updatePaymentStatus(1L, PaymentStatus.COMPLETED);

        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void refundPayment_Success() {
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO refundDTO = new PaymentResponseDTO();
        refundDTO.setStatus("REFUNDED");
        refundDTO.setRelatedPaymentId(1L);
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(refundDTO);

        PaymentResponseDTO result = paymentService.refundPayment(1L);

        assertEquals("REFUNDED", result.getStatus());
    }

    @Test
    void refundPayment_NotCompleted_ThrowsException() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class, () -> paymentService.refundPayment(1L));
    }

    @Test
    void deletePayment_Success() {
        when(paymentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(1L);

        assertDoesNotThrow(() -> paymentService.deletePayment(1L));
        verify(paymentRepository).deleteById(1L);
    }

    @Test
    void deletePayment_NotFound() {
        when(paymentRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> paymentService.deletePayment(99L));
    }
}
