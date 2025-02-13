package no.vebb.f1.util;

import java.time.Instant;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
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

	public Instant getDefaultInstant(Year year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year.value);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.toInstant();
	}
}
