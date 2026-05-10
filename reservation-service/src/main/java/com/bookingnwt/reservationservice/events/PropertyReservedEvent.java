package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event primljen od Property Service-a koji potvrđuje da je nekretnina rezervirana.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyReservedEvent {
    private Long propertyId;
    private Long reservationId;
    private Long userId;
    private int quantity;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private LocalDateTime eventTimestamp;
    private String status;  // CONFIRMED, FAILED
}
