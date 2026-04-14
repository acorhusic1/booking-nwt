package com.bookingnwt.reservationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReportRequestDTO {

    @NotNull(message = "Reservation ID je obavezan")
    private Long reservationId;

    @NotNull(message = "Reporter ID je obavezan")
    private Long reporterId;

    @NotBlank(message = "Kategorija je obavezna")
    private String category;

    @NotBlank(message = "Opis problema je obavezan")
    private String description;
}
