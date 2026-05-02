package com.bookingnwt.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class WalletTransactionRequestDTO {

    @NotNull(message = "ID novčanika je obavezan")
    private Long walletId;

    @NotNull(message = "Iznos je obavezan")
    @DecimalMin(value = "0.01", message = "Iznos mora biti veći od 0")
    private BigDecimal amount;

    @NotBlank(message = "Tip transakcije je obavezan")
    private String type;

    private String description;

    private Long paymentId;
}
