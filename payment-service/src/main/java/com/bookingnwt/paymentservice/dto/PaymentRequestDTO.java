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
public class PaymentRequestDTO {

    @NotNull(message = "ID rezervacije je obavezan")
    private Long reservationId;

    @NotNull(message = "ID gosta je obavezan")
    private Long guestId;

    @NotNull(message = "Iznos je obavezan")
    @DecimalMin(value = "0.01", message = "Iznos mora biti veći od 0")
    private BigDecimal amount;

    @NotBlank(message = "Valuta je obavezna")
    private String currency;

    @NotBlank(message = "Metoda plaćanja je obavezna")
    private String method;
}
