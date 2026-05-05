package com.bookingnwt.paymentservice.service;

import com.bookingnwt.paymentservice.client.ReservationStatusGateway;
import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.exception.ResourceNotFoundException;
import com.bookingnwt.paymentservice.mapper.PaymentMapper;
import com.bookingnwt.paymentservice.model.Payment;
import com.bookingnwt.paymentservice.model.PaymentStatus;
import com.bookingnwt.paymentservice.repository.PaymentRepository;
import com.bookingnwt.paymentservice.service.impl.PaymentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAdvancedServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private ReservationStatusGateway reservationStatusGateway;

    @InjectMocks
    private PaymentServiceImpl service;

    private Payment entity;
    private PaymentResponseDTO dto;

    @BeforeEach
    void setUp() {
        entity = new Payment(10L, 4L, new BigDecimal("200.00"), "BAM", "WALLET");
        entity.setId(1L);
        entity.setStatus(PaymentStatus.COMPLETED);

        dto = new PaymentResponseDTO();
        dto.setId(1L);
        dto.setReservationId(10L);
        dto.setGuestId(4L);
        dto.setAmount(new BigDecimal("200.00"));
        dto.setCurrency("BAM");
        dto.setStatus("COMPLETED");
        dto.setMethod("WALLET");
    }

    @Test
    void patchPayment_replaceMethod_persistsChange() throws Exception {
        ObjectMapper realMapper = JsonMapper.builder().findAndAddModules().build();
        PaymentServiceImpl real = new PaymentServiceImpl(
                paymentRepository, paymentMapper, realMapper, reservationStatusGateway);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(dto);
        when(paymentRepository.save(any(Payment.class))).thenReturn(entity);

        var patch = realMapper.readTree("[{\"op\":\"replace\",\"path\":\"/method\",\"value\":\"CARD\"}]");
        real.patchPayment(1L, patch);

        assertThat(entity.getMethod()).isEqualTo("CARD");
    }

    @Test
    void patchPayment_invalidOp_throws() throws Exception {
        ObjectMapper realMapper = JsonMapper.builder().findAndAddModules().build();
        PaymentServiceImpl real = new PaymentServiceImpl(
                paymentRepository, paymentMapper, realMapper, reservationStatusGateway);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        var bad = realMapper.readTree("[{\"op\":\"unknown_op\",\"path\":\"/x\",\"value\":1}]");

        assertThatThrownBy(() -> real.patchPayment(1L, bad))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void patchPayment_notFound_throws() throws Exception {
        ObjectMapper realMapper = JsonMapper.builder().findAndAddModules().build();
        PaymentServiceImpl real = new PaymentServiceImpl(
                paymentRepository, paymentMapper, realMapper, reservationStatusGateway);

        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
        var patch = realMapper.readTree("[{\"op\":\"replace\",\"path\":\"/method\",\"value\":\"CARD\"}]");

        assertThatThrownBy(() -> real.patchPayment(99L, patch))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPaymentsByGuestPaged_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        when(paymentRepository.findByGuestId(eq(4L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(paymentMapper.toDTO(entity)).thenReturn(dto);

        Page<PaymentResponseDTO> result = service.getPaymentsByGuestPaged(4L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getTotalSpentByGuest_nullCoercedToZero() {
        when(paymentRepository.totalSpentByGuest(4L)).thenReturn(null);
        assertThat(service.getTotalSpentByGuest(4L)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalSpentByGuest_returnsValue() {
        when(paymentRepository.totalSpentByGuest(4L)).thenReturn(new BigDecimal("250.50"));
        assertThat(service.getTotalSpentByGuest(4L)).isEqualByComparingTo("250.50");
    }

    @Test
    void findByStatusAndMinAmount_negative_throws() {
        assertThatThrownBy(() -> service.findByStatusAndMinAmount(PaymentStatus.COMPLETED, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByStatusAndMinAmount_returnsList() {
        when(paymentRepository.findByStatusAndMinAmount(eq(PaymentStatus.COMPLETED), any(BigDecimal.class)))
                .thenReturn(List.of(entity));
        when(paymentMapper.toDTO(entity)).thenReturn(dto);

        var result = service.findByStatusAndMinAmount(PaymentStatus.COMPLETED, new BigDecimal("100"));
        assertThat(result).hasSize(1);
    }

    @Test
    void countByStatus_returnsCount() {
        when(paymentRepository.countByStatus(PaymentStatus.PENDING)).thenReturn(3L);
        assertThat(service.countByStatus(PaymentStatus.PENDING)).isEqualTo(3L);
    }

    @Test
    void batchCreate_emptyList_throws() {
        assertThatThrownBy(() -> service.batchCreate(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void batchCreate_savesAll() {
        PaymentRequestDTO req = new PaymentRequestDTO();
        req.setReservationId(10L);
        req.setGuestId(4L);
        req.setAmount(new BigDecimal("200.00"));
        req.setCurrency("BAM");
        req.setMethod("WALLET");

        when(paymentMapper.toEntity(any(PaymentRequestDTO.class))).thenReturn(entity);
        when(paymentRepository.saveAll(anyList())).thenReturn(List.of(entity, entity));
        when(paymentMapper.toDTO(entity)).thenReturn(dto);

        var result = service.batchCreate(List.of(req, req));
        assertThat(result).hasSize(2);
    }

    @Test
    void getPaymentWithDetails_notFound_throws() {
        when(paymentRepository.findWithDetailsById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPaymentWithDetails(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPaymentWithDetails_returnsDto() {
        when(paymentRepository.findWithDetailsById(1L)).thenReturn(Optional.of(entity));
        when(paymentMapper.toDTO(entity)).thenReturn(dto);
        assertThat(service.getPaymentWithDetails(1L).getId()).isEqualTo(1L);
    }

    @Test
    void updatePaymentStatus_completed_callsReservationGateway() {
        Payment pending = new Payment(10L, 4L, new BigDecimal("200.00"), "BAM", "WALLET");
        pending.setId(1L);
        pending.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(dto);

        service.updatePaymentStatus(1L, PaymentStatus.COMPLETED);

        verify(reservationStatusGateway).updateStatus(10L, "CONFIRMED");
    }

    @Test
    void updatePaymentStatus_failed_callsReservationGatewayWithCancelled() {
        Payment pending = new Payment(10L, 4L, new BigDecimal("200.00"), "BAM", "WALLET");
        pending.setId(1L);
        pending.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(dto);

        service.updatePaymentStatus(1L, PaymentStatus.FAILED);

        verify(reservationStatusGateway).updateStatus(10L, "CANCELLED");
    }

    @Test
    void refundPayment_notifiesReservationGateway() {
        Payment completed = new Payment(10L, 4L, new BigDecimal("200.00"), "BAM", "WALLET");
        completed.setId(1L);
        completed.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completed));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) p.setId(2L);
            return p;
        });
        when(paymentMapper.toDTO(any(Payment.class))).thenReturn(dto);

        service.refundPayment(1L);

        verify(reservationStatusGateway).updateStatus(10L, "CANCELLED");
    }
}
