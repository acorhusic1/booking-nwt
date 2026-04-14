package com.bookingnwt.reservationservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPolicyRequestDTO {

    @NotNull(message = "Property ID je obavezan")
    private Long propertyId;

    @NotBlank(message = "Naziv je obavezan")
    private String name;

    @NotNull(message = "Broj besplatnih dana otkazivanja je obavezan")
    @Min(value = 0, message = "Minimalno 0 dana")
    private Integer freeCancelDays;

    private Integer partialRefundPct;

    private Boolean noRefund;
}
