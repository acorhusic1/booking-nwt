package com.bookingnwt.reservationservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * F9 — Podsjetnik gostu dan prije check-in-a, ILI zahtjev za recenziju
 * gostu dan poslije check-out-a. Tip se razlikuje kroz eventType polje.
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
    private String eventType; // "RESERVATION_REMINDER" ili "REVIEW_REQUEST"
}
