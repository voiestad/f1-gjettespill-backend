package no.vebb.f1.database;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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

	public int getLatestRaceNumber(int year) throws EmptyResultDataAccessException {
		final String getRaceNumberSql = """
			SELECT ro.id
			FROM RaceOrder ro
			JOIN Sprint s ON ro.id = s.race_number
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1;
		""";

		return jdbcTemplate.queryForObject(getRaceNumberSql, Integer.class, year);	
	}

	public int getPositionOfRace(int raceNumber) {
		final String getRacePosition = "SELECT position FROM RaceOrder WHERE id = ?";
		return jdbcTemplate.queryForObject(getRacePosition, Integer.class, raceNumber);
	}

	public Map<Integer, Integer> getDiffMap(String category, int year) {
		final String sql = "SELECT diff, points FROM DiffPointsMap WHERE category = ? and year = ?";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, category, year);
		Map<Integer, Integer> map = new HashMap<>();
		for (Map<String, Object> entry : result) {
			Integer diff = (Integer) entry.get("diff");
			Integer points = (Integer) entry.get("points");
			map.put(diff, points);
		}
		return map;
	}

	public String translateCategory(String category) {
		final String translationSql = """
				SELECT translation
				FROM CategoryTranslation
				WHERE category = ?
				""";

		return jdbcTemplate.queryForObject(translationSql, String.class, category);
	}

	public String translateFlagName(String flag) {
		final String translationSql = """
				SELECT translation
				FROM FlagTranslation
				WHERE flag = ?
				""";

		return jdbcTemplate.queryForObject(translationSql, String.class, flag);
	}

	public List<Map<String, Object>> getDataForFlagTable(int racePos, int year, UUID guesserId) {
		if (racePos == 0) {
			final String sqlNoRace = """
			SELECT f.name AS type, fg.amount AS guessed, 0 AS actual
			FROM Flag f
			JOIN FlagGuess fg ON f.name = fg.flag
			JOIN RaceOrder ro ON fg.year = ro.year
			WHERE ro.year = ? AND fg.guesser = ?
			GROUP BY f.name
			""";
			return jdbcTemplate.queryForList(sqlNoRace, year, guesserId);
		} else {
			final String sql = """
				SELECT f.name AS type, fg.amount AS guessed, COALESCE(COUNT(fs.flag), 0) AS actual
				FROM Flag f
				JOIN FlagGuess fg ON f.name = fg.flag
				JOIN RaceOrder ro ON fg.year = ro.year
				LEFT JOIN FlagStats fs ON fs.flag = f.name AND fs.race_number = ro.id
				WHERE ro.year = ? AND fg.guesser = ? AND ro.position <= ?
				GROUP BY f.name
				""";
			return jdbcTemplate.queryForList(sql, year, guesserId, racePos);
		}
	}

	public List<Map<String, Object>> getDataForPlaceGuessTable(String category, UUID guesserId, int year, int racePos) {
		final String sql = """
		SELECT r.name AS race_name, dpg.driver AS driver, sg.position AS start, rr.finishing_position AS finish
		FROM DriverPlaceGuess dpg
		JOIN Race r ON r.id = dpg.race_number
		JOIN RaceOrder ro ON r.id = ro.id
		JOIN StartingGrid sg ON sg.race_number = r.id AND dpg.driver = sg.driver
		JOIN RaceResult rr ON rr.race_number = r.id AND dpg.driver = rr.driver
		WHERE dpg.category = ? AND dpg.guesser = ? AND ro.year = ? AND ro.position <= ?
		ORDER BY ro.position ASC
		""";
		return jdbcTemplate.queryForList(sql, category, guesserId, year, racePos);
	}

	public List<String> getGuessedYearDriver(int year, UUID guesserId) {
		final String guessedSql = "SELECT driver FROM DriverGuess WHERE year = ?  AND guesser = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(guessedSql, String.class, year, guesserId);
	}

	public List<String> getGuessedYearConstructor(int year, UUID guesserId) {
		final String guessedSql = "SELECT constructor FROM ConstructorGuess WHERE year = ? AND guesser = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(guessedSql, String.class, year, guesserId);
	}

	public List<String> getDriverStandings(int raceNumber, int year) {
		final String driverYearSql = "SELECT driver FROM DriverYear WHERE year = ? ORDER BY position ASC";
		final String driverStandingsSql = "SELECT driver FROM DriverStandings WHERE race_number = ? ORDER BY position ASC";
		if (raceNumber == -1) {
			return jdbcTemplate.queryForList(driverYearSql, String.class, year);
		} else {
			return jdbcTemplate.queryForList(driverStandingsSql, String.class, raceNumber);
		}
	}

	public List<String> getConstructorStandings(int raceNumber, int year) {
		final String constructorYearSql = "SELECT constructor FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		final String constructorStandingsSql = "SELECT constructor FROM ConstructorStandings WHERE race_number = ? ORDER BY position ASC";
		if (raceNumber == -1) {
			return jdbcTemplate.queryForList(constructorYearSql, String.class, year);
		} else {
			return jdbcTemplate.queryForList(constructorStandingsSql, String.class, raceNumber);
		}
	}

}
