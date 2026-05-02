package com.bookingnwt.analyticsservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RevenueReportRequestDTO {

    @NotNull(message = "ID domaćina je obavezan")
    private Long hostId;

    @NotNull(message = "Godina je obavezna")
    private Integer year;

    @NotNull(message = "Mjesec je obavezan")
    private Integer month;

    @NotNull(message = "Ukupni prihod je obavezan")
    private BigDecimal totalRevenue;

    @NotNull(message = "Provizija platforme je obavezna")
    private BigDecimal platformCommission;

    @NotNull(message = "Neto prihod je obavezan")
    private BigDecimal netRevenue;

    @NotNull(message = "Ukupan broj rezervacija je obavezan")
    private Integer totalReservations;

    @NotNull(message = "Ukupan broj otkazivanja je obavezan")
    private Integer totalCancellations;

    @NotNull(message = "Ukupan broj objekata je obavezan")
    private Integer totalProperties;

    private BigDecimal averageOccupancyRate;
}
