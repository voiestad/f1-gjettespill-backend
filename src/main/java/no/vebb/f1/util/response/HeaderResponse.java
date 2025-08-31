package no.vebb.f1.util.response;

import no.vebb.f1.util.collection.Race;

public class HeaderResponse {
	public boolean isLoggedIn;
	public boolean isRaceGuess;
	public boolean isAbleToGuess;
	public boolean isAdmin;
	public Race ongoingRace;
}
