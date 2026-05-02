package com.bookingnwt.analyticsservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RevenueReportResponseDTO {

    private Long id;
    private Long hostId;
    private Integer year;
    private Integer month;
    private BigDecimal totalRevenue;
    private BigDecimal platformCommission;
    private BigDecimal netRevenue;
    private Integer totalReservations;
    private Integer totalCancellations;
    private Integer totalProperties;
    private BigDecimal averageOccupancyRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
