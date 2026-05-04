package com.bookingnwt.reservationservice.exception;

/**
 * Thrown when reservation-service cannot confirm that a property is bookable
 * for the requested window — either the property does not exist, is inactive,
 * is blocked on the calendar, or property-service itself is unreachable
 * (fail-closed: we never silently allow an unverified booking).
 */
public class PropertyUnavailableException extends RuntimeException {
    public PropertyUnavailableException(String message) {
        super(message);
    }
}
