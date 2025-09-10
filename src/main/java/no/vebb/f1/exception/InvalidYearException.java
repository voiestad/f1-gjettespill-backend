package no.vebb.f1.exception;

public class InvalidYearException extends RuntimeException {
	public InvalidYearException(String message) {
		super(message);
	}
	public InvalidYearException() {
		super();
	}
}
