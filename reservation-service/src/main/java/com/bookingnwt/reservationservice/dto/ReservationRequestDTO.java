package com.bookingnwt.reservationservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {

    @NotNull(message = "Guest ID je obavezan")
    private Long guestId;

    @NotNull(message = "Host ID je obavezan")
    private Long hostId;

    @NotNull(message = "Property ID je obavezan")
    private Long propertyId;

    // BUG fix — @Future je odbijao DANASNJI check-in iako frontend datum-picker
    // dozvoljava danas (min=danas). To je davalo zbunjujuci 400 "mora biti u
    // buducnosti". @FutureOrPresent dozvoljava check-in danas; ispravan
    // redoslijed (checkOut > checkIn) provjerava se u servisu sa jasnom porukom.
    @NotNull(message = "Datum check-in je obavezan")
    @FutureOrPresent(message = "Datum dolaska ne može biti u prošlosti")
    private LocalDate checkIn;

    @NotNull(message = "Datum check-out je obavezan")
    @FutureOrPresent(message = "Datum odlaska ne može biti u prošlosti")
    private LocalDate checkOut;

    @NotNull(message = "Broj gostiju je obavezan")
    @Min(value = 1, message = "Minimalno 1 gost")
    private Integer numGuests;

    @NotNull(message = "Ukupna cijena je obavezna")
    @DecimalMin(value = "0.01", message = "Cijena mora biti veća od 0")
    @DecimalMax(value = "100000.00", message = "Cijena ne smije prelaziti 100,000 BAM")
    private BigDecimal totalPrice;

    private Long cancellationPolicyId;

    private Long promoCodeId;
}
