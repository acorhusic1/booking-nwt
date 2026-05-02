package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalRuleRequest {

    @NotBlank(message = "Naziv pravila je obavezan")
    private String name;

    @NotNull(message = "Datum početka je obavezan")
    private LocalDate startDate;

    @NotNull(message = "Datum kraja je obavezan")
    private LocalDate endDate;

    @NotNull(message = "Modifikator cijene je obavezan")
    private Integer priceModifierPct;

    private Integer minNights;
}
