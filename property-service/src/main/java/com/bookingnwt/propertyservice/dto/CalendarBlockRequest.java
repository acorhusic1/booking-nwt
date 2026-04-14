package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarBlockRequest {

    @NotNull(message = "Datum početka je obavezan")
    private LocalDate startDate;

    @NotNull(message = "Datum kraja je obavezan")
    private LocalDate endDate;

    private String reason;

    private Long createdBy;
}
