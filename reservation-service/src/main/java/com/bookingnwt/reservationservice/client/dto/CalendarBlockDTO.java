package com.bookingnwt.reservationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarBlockDTO {
    private Long id;
    private Long propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Long createdBy;
}
