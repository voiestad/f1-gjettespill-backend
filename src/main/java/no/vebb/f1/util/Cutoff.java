package no.vebb.f1.util;

import java.time.Instant;
import java.time.Year;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class Cutoff {
	
	private JdbcTemplate jdbcTemplate;

	public Cutoff(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public boolean isAbleToGuessCurrentYear() {
		return isAbleToGuessYear(getCurrentYear());	
	}

	public boolean isAbleToGuessYear(int year) {
		final String existCheck = "SELECT COUNT(*) FROM YearCutoff WHERE year = ?";
		boolean yearExist = jdbcTemplate.queryForObject(existCheck, Integer.class, getCurrentYear()) > 0;
		if (!yearExist) {
			return false;
		}
		final String getCutoff = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, (rs, rowNum) -> rs.getString("cutoff"), getCurrentYear()));

		return isAbleToGuess(cutoff);
	}

	public boolean isAbleToGuessRace(int raceNumber) {
		final String getCutoff = "SELECT cutoff FROM RaceCutoff WHERE race_number = ?";
		Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceNumber));
		return isAbleToGuess(cutoff);
	}

	public int getCurrentYear() {
		return Year.now().getValue();
	}

	private boolean isAbleToGuess(Instant cutoff) {
		Instant now = Instant.now();
		return cutoff.compareTo(now) > 0;
	}

}
