package com.flight.app.exception;

// Used when a flight is found but doesn't have enough seats
public class FlightUnavailableException extends RuntimeException {
	public FlightUnavailableException(String message) {
	    super(message);
	}
}
