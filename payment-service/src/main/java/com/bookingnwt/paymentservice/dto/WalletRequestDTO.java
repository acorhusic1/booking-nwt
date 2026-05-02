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
public class WalletRequestDTO {

    @NotNull(message = "ID korisnika je obavezan")
    private Long userId;

    @NotNull(message = "Početni balans je obavezan")
    @DecimalMin(value = "0.00", message = "Balans ne može biti negativan")
    private BigDecimal balance;

    @NotBlank(message = "Valuta je obavezna")
    private String currency;
}
