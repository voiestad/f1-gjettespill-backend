package no.vebb.f1.exception;

public class NoAvailableRaceException extends Exception {
	public NoAvailableRaceException(String e) {
		super(e);
	}
	public NoAvailableRaceException() {
		super();
	}
}
