package com.bookingnwt.analyticsservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PropertyStatisticsResponseDTO {

    private Long id;
    private Long propertyId;
    private Long hostId;
    private Integer year;
    private Integer month;
    private Integer totalReservations;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private BigDecimal occupancyRate;
    private Integer viewCount;
    private Integer cancellationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
