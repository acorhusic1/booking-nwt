package com.bookingnwt.paymentservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class WalletTransactionResponseDTO {

    private Long id;
    private Long walletId;
    private BigDecimal amount;
    private String type;
    private String description;
    private Long paymentId;
    private LocalDateTime createdAt;
}
