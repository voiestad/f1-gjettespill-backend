package no.voiestad.f1.guessing.collection;

import no.voiestad.f1.competitors.domain.DriverName;

public record UserQualifyingGuess(String username, DriverName driver) {
	public static UserQualifyingGuess fromIUserQualifyingGuess(IUserQualifyingGuess iUserQualifyingGuess) {
		return new UserQualifyingGuess(iUserQualifyingGuess.getUsername(),
				new DriverName(iUserQualifyingGuess.getDriverName()));
	}
}
