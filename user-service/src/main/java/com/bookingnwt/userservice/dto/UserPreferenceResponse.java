package com.bookingnwt.userservice.dto;

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
public class UserPreferenceResponse {
    private Long id;
    private Long userId;
    private String preferredLanguage;
    private String propertyType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDateTime updatedAt;
}
