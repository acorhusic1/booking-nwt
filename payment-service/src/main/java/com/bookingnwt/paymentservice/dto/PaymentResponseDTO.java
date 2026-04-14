package com.bookingnwt.paymentservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponseDTO {

    private Long id;
    private Long reservationId;
    private Long guestId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String method;
    private LocalDateTime processedAt;
    private Long relatedPaymentId;
}
