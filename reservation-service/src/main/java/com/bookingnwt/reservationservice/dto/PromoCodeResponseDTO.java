package com.bookingnwt.reservationservice.dto;

import com.bookingnwt.reservationservice.model.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponseDTO {

    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer minNights;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxUses;
    private Integer usageCount;
    private Long createdBy;
    private LocalDateTime createdAt;
}
