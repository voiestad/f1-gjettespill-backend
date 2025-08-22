package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidUsernameException;

public class Username {
	
	public final String username;
	private final Database db;

	public Username(String username, Database db) throws InvalidUsernameException {
		username = username.strip();
		this.username = username;
		this.db = db;
		validate();
	}

	private void validate() throws InvalidUsernameException {
		if (username.isEmpty()) {
			throw new InvalidUsernameException("Brukernavn kan ikke være blankt.");
		}

		if (!username.matches("^[a-zA-ZÆØÅæøå ]+$")) {
			throw new InvalidUsernameException("Brukernavn kan bare inneholde (a-å, A-Å).");
		}
		
		if (username.equalsIgnoreCase("ANONYM")) {
			throw new InvalidUsernameException("Brukernavn kan ikke være 'Anonym'.");
		}

		boolean isUsernameInUse = db.isUsernameInUse(username);

		if (isUsernameInUse) {
			throw new InvalidUsernameException("Brukernavnet er allerede i bruk. Vennligst velg et annet.");
		}
	}
}
