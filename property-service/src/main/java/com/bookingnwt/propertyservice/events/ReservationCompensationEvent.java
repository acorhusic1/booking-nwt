package com.bookingnwt.propertyservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event koji se emituje kada trebamo vratiti sistem u inicijalno stanje
 * (Kompenzaciona akcija)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCompensationEvent {
    private Long reservationId;
    private Long propertyId;
    private String reason;
    private boolean compensated;
}

