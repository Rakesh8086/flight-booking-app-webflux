package com.flight.app.exception;

public class CancellationNotPossibleException extends RuntimeException{
	public CancellationNotPossibleException(String message) {
	    super(message);
	}
}
