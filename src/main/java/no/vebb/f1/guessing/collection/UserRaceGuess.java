package no.vebb.f1.guessing.collection;

import no.vebb.f1.competitors.domain.DriverName;
import no.vebb.f1.results.domain.CompetitorPosition;

public class UserRaceGuess {
	
	public final String user;
	public final DriverName driver;
	public final CompetitorPosition position;

	public UserRaceGuess(String username, DriverName driver, CompetitorPosition position) {
		this.user = username;
		this.driver = driver;
		this.position = position;
	}

	public static UserRaceGuess fromIUserRaceGuess(IUserRaceGuess iUserRaceGuess) {
		return new UserRaceGuess(iUserRaceGuess.getUsername(), new DriverName(iUserRaceGuess.getDriverName()),
				iUserRaceGuess.getStartPosition());
	}
}
