package com.bookingnwt.propertyservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Event koji se emituje kada se znanje uspješno rezervira
 * Slušan od strane ostalih servisa koji trebaju biti notifikovani
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyReservedEvent {
    private Long propertyId;           // Koje znanje je rezervirano
    private Long reservationId;        // Koja rezervacija
    private Long userId;               // Koji korisnik je pravom
    private int quantity;              // Koliko dostupnih jedinica je zauzeto
    private LocalDateTime checkInDate;  // Datum dolaska
    private LocalDateTime checkOutDate; // Datum odlaska
    private LocalDateTime eventTimestamp;
    private String status;             // NEW, CONFIRMED, FAILED
}

