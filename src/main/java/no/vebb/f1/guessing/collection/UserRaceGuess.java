package no.vebb.f1.guessing.collection;

import no.vebb.f1.competitors.domain.Driver;

public class UserRaceGuess {
	
	public final String user;
	public final Driver driver;
	public final int position;

	public UserRaceGuess(String username, Driver driver, int position) {
		this.user = username;
		this.driver = driver;
		this.position = position;
	}

	public static UserRaceGuess fromIUserRaceGuess(IUserRaceGuess iUserRaceGuess) {
		return new UserRaceGuess(iUserRaceGuess.getUsername(), new Driver(iUserRaceGuess.getDriverName()),
				iUserRaceGuess.getStartPosition());
	}
}
