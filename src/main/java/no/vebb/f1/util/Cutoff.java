package no.vebb.f1.util;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;

@Service
public class Cutoff {
	
	@Autowired
	private Database db;


	public boolean isAbleToGuessCurrentYear() {
		return isAbleToGuessYear(TimeUtil.getCurrentYear());	
	}

	public boolean isAbleToGuessYear(int year) {
		boolean yearExist = db.yearCutOffExist(year);
		if (!yearExist) {
			return false;
		}
		Instant cutoff = db.getCutoffYear(year);
		return isAbleToGuess(cutoff);
	}

	public boolean isAbleToGuessRace(int raceNumber) throws NoAvailableRaceException {
		Instant cutoff = db.getCutoffRace(raceNumber);
		return isAbleToGuess(cutoff);
	}

	private boolean isAbleToGuess(Instant cutoff) {
		Instant now = Instant.now();
		return cutoff.compareTo(now) > 0;
	}

}
