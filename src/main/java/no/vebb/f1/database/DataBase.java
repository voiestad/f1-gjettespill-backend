package no.vebb.f1.database;

import java.time.Instant;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.NoAvailableRaceException;

@Service
public class Database {
	
	private JdbcTemplate jdbcTemplate;

	public Database(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean yearCutOffExist(int year) {
		final String existCheck = "SELECT COUNT(*) FROM YearCutoff WHERE year = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, year) > 0;
	}

	public Instant getCutoffYear(int year) {
		final String getCutoff = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, year));
	}

	public Instant getCutoffRace(int raceNumber) throws NoAvailableRaceException {
		try {
			final String getCutoff = "SELECT cutoff FROM RaceCutoff WHERE race_number = ?";
			return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceNumber));
		} catch (EmptyResultDataAccessException e) {
			throw new NoAvailableRaceException("There is no cutoff for the given raceNumber '" + raceNumber + "'");
		}
	}

}
