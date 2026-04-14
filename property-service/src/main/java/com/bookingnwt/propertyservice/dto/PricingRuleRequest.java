package com.bookingnwt.propertyservice.dto;

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
public class PricingRuleRequest {

    @NotNull(message = "Osnovna cijena je obavezna")
    @DecimalMin(value = "0.0", message = "Cijena mora biti >= 0")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", message = "Vikend cijena mora biti >= 0")
    private BigDecimal weekendPrice;

    private Integer minStayDays;
    private Integer maxStayDays;
    private Integer longStayDiscountPct;
    private Integer longStayThreshold;
}
