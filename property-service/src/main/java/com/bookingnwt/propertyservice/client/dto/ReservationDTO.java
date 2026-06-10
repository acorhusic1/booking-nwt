package com.bookingnwt.propertyservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Minimalni pogled na rezervaciju iz reservation-service — koristi se za F7
 * validaciju recenzija (samo gost sa zavrsenom rezervacijom smije ocijeniti).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationDTO {
    private Long id;
    private Long guestId;
    private Long hostId;
    private Long propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status;
}
