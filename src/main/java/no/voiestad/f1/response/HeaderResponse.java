package no.voiestad.f1.response;

import no.voiestad.f1.collection.Race;

public class HeaderResponse {
	public boolean isLoggedIn;
	public boolean isRaceGuess;
	public boolean isAbleToGuess;
	public boolean isAdmin;
	public Race ongoingRace;
}
