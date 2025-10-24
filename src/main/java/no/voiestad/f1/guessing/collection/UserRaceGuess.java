package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.results.domain.CompetitorPosition;

public record UserRaceGuess(String username, DriverName driver, CompetitorPosition position) {
	public static UserRaceGuess fromIUserRaceGuess(IUserRaceGuess iUserRaceGuess) {
		return new UserRaceGuess(iUserRaceGuess.getUsername(), new DriverName(iUserRaceGuess.getDriverName()),
				iUserRaceGuess.getStartPosition());
	}
}
