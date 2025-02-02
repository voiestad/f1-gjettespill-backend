package no.vebb.f1.util;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;

@Service
public class Cutoff {
	
	@Autowired
	private Database db;


	public boolean isAbleToGuessCurrentYear() {
		try {
			return isAbleToGuessYear(new Year(TimeUtil.getCurrentYear(), db));	
		} catch (InvalidYearException e) {
			return false;
		}
	}

	public boolean isAbleToGuessYear(Year year) {
			boolean yearExist = db.yearCutOffExist(year);
			if (!yearExist) {
				return false;
			}
			Instant cutoff = db.getCutoffYear(year);
			return isAbleToGuess(cutoff);
	}

	public boolean isAbleToGuessRace(RaceId raceId) throws NoAvailableRaceException {
		Instant cutoff = db.getCutoffRace(raceId);
		return isAbleToGuess(cutoff);
	}

	private boolean isAbleToGuess(Instant cutoff) {
		Instant now = Instant.now();
		return cutoff.compareTo(now) > 0;
	}

}
