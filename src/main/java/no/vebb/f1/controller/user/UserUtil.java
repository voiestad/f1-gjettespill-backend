package no.vebb.f1.controller.user;

import no.vebb.f1.database.Database;

public class UserUtil {
	
	public static String validateUsername(String username, Database db) {
		if (username.equals("")) {
			return "Brukernavn kan ikke være blankt.";
		}

		if (!username.matches("^[a-zA-ZÆØÅæøå ]+$")) {
			return "Brukernavn kan bare inneholde (a-å, A-Å).";
		}

		String username_upper = username.toUpperCase();
		
		if (username_upper.equals("ANONYM")) {
			return "Brukernavn kan ikke være 'Anonym'";
		}

		boolean isUsernameInUse = db.isUsernameInUse(username_upper);

		if (isUsernameInUse) {
			return "Brukernavnet er allerede i bruk. Vennligst velg et annet.";
		}
		
		return null;
	}
}
