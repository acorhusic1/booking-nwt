package com.bookingnwt.propertyservice.dto;

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
public class ReviewRequest {

    @NotNull(message = "Reservation ID je obavezan")
    private Long reservationId;

    @NotNull(message = "Guest ID je obavezan")
    private Long guestId;

    @NotNull(message = "Property ID je obavezan")
    private Long propertyId;

    @NotNull(message = "Host ID je obavezan")
    private Long hostId;

    @NotNull(message = "Ocjena čistoće je obavezna")
    private BigDecimal ratingCleanliness;

    @NotNull(message = "Ocjena lokacije je obavezna")
    private BigDecimal ratingLocation;

    @NotNull(message = "Ocjena komunikacije je obavezna")
    private BigDecimal ratingCommunication;

    @NotNull(message = "Ocjena vrijednosti je obavezna")
    private BigDecimal ratingValue;

    @NotNull(message = "Ocjena tačnosti je obavezna")
    private BigDecimal ratingAccuracy;

    private String comment;
}
