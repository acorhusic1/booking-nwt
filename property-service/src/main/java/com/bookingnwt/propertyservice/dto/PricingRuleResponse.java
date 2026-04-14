package com.bookingnwt.propertyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleResponse {
    private Long id;
    private Long propertyId;
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private Integer minStayDays;
    private Integer maxStayDays;
    private Integer longStayDiscountPct;
    private Integer longStayThreshold;
    private LocalDateTime createdAt;
}
