package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event za kompenzaciju — vraćanje sistema u inicijalno stanje.
 * Primljen od Property Service-a kada treba otkazati rezervaciju.
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
