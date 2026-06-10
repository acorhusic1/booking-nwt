package com.bookingnwt.notificationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * F17 — event iz reservation-service: gost je prijavio problem tokom boravka.
 * notification-service kreira notifikaciju domaćinu.
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
