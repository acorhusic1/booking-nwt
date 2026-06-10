package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * F17 — event koji reservation-service emituje kada gost prijavi problem
 * tokom boravka. notification-service sluša i kreira notifikaciju domaćinu
 * (dokumentacija: "Domaćin dobija notifikaciju i ima definisan rok za
 * odgovor/rješavanje problema").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReportedEvent {
    private Long reportId;
    private Long reservationId;
    private Long propertyId;
    private Long hostId;
    private Long reporterId;
    private String category;
    private String description;
    private LocalDateTime eventTimestamp;
    private String eventType = "PROBLEM_REPORTED";
}
