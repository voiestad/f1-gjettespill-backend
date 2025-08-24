package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.user.UserRespository;
import no.vebb.f1.util.exception.InvalidUsernameException;

public class Username {
	
	public final String username;
	private final UserRespository userRespository;

	public Username(String username, UserRespository userRespository) throws InvalidUsernameException {
		this.username = username.strip();
		this.userRespository = userRespository;
		validate();
	}

	private void validate() throws InvalidUsernameException {
		if (username.isEmpty()) {
			throw new InvalidUsernameException("Brukernavn kan ikke være blankt.");
		}

		if (!username.matches("^[a-zA-ZÆØÅæøå ]+$")) {
			throw new InvalidUsernameException("Brukernavn kan bare inneholde (a-å, A-Å).");
		}
		
		if (username.length() > 30) {
			throw new InvalidUsernameException("Brukernavn kan maksimalt inneholde 30 tegn.");
		}

		if (username.length() >= 6 && username.substring(0, 6).equalsIgnoreCase("anonym")) {
			throw new InvalidUsernameException("Brukernavn kan ikke start med 'Anonym'.");
		}

		boolean isUsernameInUse = userRespository.findByUsername(username).isPresent();

		if (isUsernameInUse) {
			throw new InvalidUsernameException("Brukernavnet er allerede i bruk. Vennligst velg et annet.");
		}
	}
}
