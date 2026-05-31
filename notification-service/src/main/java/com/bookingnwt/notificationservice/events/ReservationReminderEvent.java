package com.bookingnwt.notificationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mirror DTO za ReservationReminderEvent iz reservation-service.
 * F9 — RESERVATION_REMINDER (dan prije check-in-a) i REVIEW_REQUEST (dan poslije check-out-a).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationReminderEvent {
    private Long reservationId;
    private Long guestId;
    private Long propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalDateTime eventTimestamp;
    private String eventType;
}
