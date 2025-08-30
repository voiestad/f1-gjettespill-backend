package no.vebb.f1.util.exception;

public class InvalidYearException extends RuntimeException {
	public InvalidYearException(String message) {
		super(message);
	}
	public InvalidYearException() {
		super();
	}
}
