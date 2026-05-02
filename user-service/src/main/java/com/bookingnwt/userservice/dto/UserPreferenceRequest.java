package com.bookingnwt.userservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceRequest {

    @NotNull(message = "User ID je obavezan")
    private Long userId;

    private String preferredLanguage;

    private String propertyType;

    @DecimalMin(value = "0.0", message = "Minimalna cijena mora biti >= 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maksimalna cijena mora biti >= 0")
    private BigDecimal maxPrice;
}
