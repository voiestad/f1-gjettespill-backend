package no.vebb.f1.util.exception;

public class InvalidRaceException extends RuntimeException {
	public InvalidRaceException(String message) {
		super(message);
	}
	public InvalidRaceException() {
		super();
	}
}
