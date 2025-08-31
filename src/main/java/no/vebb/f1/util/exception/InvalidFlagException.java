package no.vebb.f1.util.exception;

public class InvalidFlagException extends RuntimeException {
	public InvalidFlagException(String message) {
		super(message);
	}
	public InvalidFlagException() {
		super();
	}
}
