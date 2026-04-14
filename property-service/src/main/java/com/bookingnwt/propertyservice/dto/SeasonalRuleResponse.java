package com.bookingnwt.propertyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalRuleResponse {
    private Long id;
    private Long propertyId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priceModifierPct;
    private Integer minNights;
    private LocalDateTime createdAt;
}
