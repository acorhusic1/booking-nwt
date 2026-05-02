package com.bookingnwt.reservationservice.dto;

import jakarta.validation.constraints.Future;
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

    @NotNull(message = "Datum check-in je obavezan")
    @Future(message = "Datum check-in mora biti u budućnosti")
    private LocalDate checkIn;

    @NotNull(message = "Datum check-out je obavezan")
    @Future(message = "Datum check-out mora biti u budućnosti")
    private LocalDate checkOut;

    @NotNull(message = "Broj gostiju je obavezan")
    @Min(value = 1, message = "Minimalno 1 gost")
    private Integer numGuests;

    @NotNull(message = "Ukupna cijena je obavezna")
    @Min(value = 0, message = "Cijena ne može biti negativna")
    private BigDecimal totalPrice;

    private Long cancellationPolicyId;

    private Long promoCodeId;
}
