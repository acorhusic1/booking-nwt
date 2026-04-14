package com.bookingnwt.paymentservice.mapper;

import com.bookingnwt.paymentservice.dto.PaymentRequestDTO;
import com.bookingnwt.paymentservice.dto.PaymentResponseDTO;
import com.bookingnwt.paymentservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "relatedPayment", ignore = true)
    @Mapping(target = "walletTransactions", ignore = true)
    Payment toEntity(PaymentRequestDTO dto);

    @Mapping(source = "relatedPayment.id", target = "relatedPaymentId")
    PaymentResponseDTO toDTO(Payment payment);
}
