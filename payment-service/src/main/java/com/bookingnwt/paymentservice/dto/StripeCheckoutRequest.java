package com.bookingnwt.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StripeCheckoutRequest {
    @NotNull
    private Long walletId;

    @NotNull
    @DecimalMin(value = "1.00", message = "Iznos mora biti barem 1 BAM")
    private BigDecimal amount;
}
