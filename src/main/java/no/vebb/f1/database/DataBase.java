package no.vebb.f1.database;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.user.User;
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

	public boolean isUserAdmin(UUID id) {
		final String sql = "SELECT COUNT(*) FROM Admin WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
	}

	public User getUserFromId(UUID id) throws EmptyResultDataAccessException {
		final String sql = "SELECT username, google_id FROM User WHERE id = ?";
		Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, id);
		String username = (String) sqlRes.get("username");
		String googleId = (String) sqlRes.get("google_id");
		return new User(googleId, id, username);
	}

	public User getUserFromGoogleId(String googleId) throws EmptyResultDataAccessException {
		final String sql = "SELECT username, id FROM User WHERE google_id = ?";
		Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, googleId);
		String username = (String) sqlRes.get("username");
		UUID id = UUID.fromString((String) sqlRes.get("id"));
		return new User(googleId, id, username);
	}

}
