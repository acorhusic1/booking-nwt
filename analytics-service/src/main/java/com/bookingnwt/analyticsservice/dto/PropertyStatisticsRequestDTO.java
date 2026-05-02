package com.bookingnwt.analyticsservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PropertyStatisticsRequestDTO {

    @NotNull(message = "ID objekta je obavezan")
    private Long propertyId;

    @NotNull(message = "ID domaćina je obavezan")
    private Long hostId;

    @NotNull(message = "Godina je obavezna")
    private Integer year;

    @NotNull(message = "Mjesec je obavezan")
    private Integer month;

    @NotNull(message = "Ukupan broj rezervacija je obavezan")
    private Integer totalReservations;

    @NotNull(message = "Ukupni prihod je obavezan")
    private BigDecimal totalRevenue;

    private BigDecimal averageRating;

    private BigDecimal occupancyRate;

    @NotNull(message = "Broj pregleda je obavezan")
    private Integer viewCount;

    @NotNull(message = "Broj otkazivanja je obavezan")
    private Integer cancellationCount;
}
