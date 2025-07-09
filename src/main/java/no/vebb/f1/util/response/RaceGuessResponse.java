package no.vebb.f1.util.response;

import java.util.List;

import no.vebb.f1.util.collection.UserRaceGuess;

public class RaceGuessResponse {
	public final String name;
	public final List<UserRaceGuess> first;
	public final List<UserRaceGuess> tenth;

	public RaceGuessResponse(String name, List<UserRaceGuess> first, List<UserRaceGuess> tenth) {
		this.name = name;
		this.first = first;
		this.tenth = tenth;
	}
	
}
