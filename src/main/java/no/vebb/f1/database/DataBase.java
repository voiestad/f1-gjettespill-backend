package no.vebb.f1.database;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.Flags;
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

	public boolean isUsernameInUse(String usernameUpper) {
		final String sqlCheckUsername = "SELECT COUNT(*) FROM User WHERE username_upper = ?";
		return jdbcTemplate.queryForObject(sqlCheckUsername, Integer.class, usernameUpper) > 0;
	}

	public void updateUsername(String username, UUID id) {
		final String updateUsername = """
				UPDATE User
				SET username = ?, username_upper = ?
				WHERE id = ?
				""";
		String usernameUpper = username.toUpperCase();
		jdbcTemplate.update(updateUsername, username, usernameUpper, id);
	}

	public void deleteUser(UUID id) {
		final String deleteUser = """
				UPDATE User
				SET username = 'Anonym', username_upper = 'ANONYM', google_id = ?
				WHERE id = ?
				""";
		jdbcTemplate.update(deleteUser, id, id);
	}

	public void addUser(String username, String googleId) {
		String username_upper = username.toUpperCase();
		final String sqlInsertUsername = "INSERT INTO User (google_id, id,username, username_upper) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(sqlInsertUsername, googleId, UUID.randomUUID(), username, username_upper);
	}

	public List<Map<String, Object>> getUserGuessesDriverPlace(int raceNumber, String category) {
		final String getGuessSql = """
					SELECT u.username AS username, dpg.driver AS driver, sg.position AS position
					FROM DriverPlaceGuess dpg
					JOIN User u ON u.id = dpg.guesser
					JOIN StartingGrid sg ON sg.race_number = dpg.race_number AND sg.driver = dpg.driver
					WHERE dpg.race_number = ? AND dpg.category = ?
					ORDER BY u.username ASC
				""";
		return jdbcTemplate.queryForList(getGuessSql, raceNumber, category);
	}

	public Map<String, Object> getLatestRaceForPlaceGuess(int year) {
		final String getRaceIdSql = """
					SELECT ro.id AS id, ro.position AS position, r.name AS name
					FROM RaceOrder ro
					JOIN StartingGrid sg ON ro.id = sg.race_number
					JOIN Race r ON r.id = ro.id
					WHERE ro.year = ?
					ORDER BY ro.position DESC
					LIMIT 1;
				""";
		return jdbcTemplate.queryForMap(getRaceIdSql, year);
	}

	public int getCurrentRaceIdToGuess() {
		final String getRaceId = """
				SELECT DISTINCT race_number
				FROM StartingGrid sg
				WHERE sg.race_number NOT IN (
					SELECT rr.race_number
					FROM RaceResult rr
				)
				""";
		return jdbcTemplate.queryForObject(getRaceId, Integer.class);
	}

	public void addFlagGuesses(UUID id, int year, Flags flags) {
		final String sql = "REPLACE INTO FlagGuess (guesser, flag, year, amount) values (?, ?, ?, ?)";
		jdbcTemplate.update(sql, id, "Yellow Flag", year, flags.yellow);
		jdbcTemplate.update(sql, id, "Red Flag", year, flags.red);
		jdbcTemplate.update(sql, id, "Safety Car", year, flags.safetyCar);
	}

	public Flags getFlagGuesses(UUID id, int year) {
		final String sql = "SELECT flag, amount FROM FlagGuess WHERE guesser = ? AND year = ?";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, id, year);
		Flags flags = new Flags();
		for (Map<String, Object> row : sqlRes) {
			String flag = (String) row.get("flag");
			int amount = (int) row.get("amount");
			switch (flag) {
				case "Yellow Flag":
					flags.yellow = amount;
					break;
				case "Red Flag":
					flags.red = amount;
					break;
				case "Safety Car":
					flags.safetyCar = amount;
					break;
			}
		}
		return flags;
	}

	public long getTimeLeftToGuessRace(int raceNumber) {
		Instant now = Instant.now();
		final String getCutoff = "SELECT cutoff FROM RaceCutoff WHERE race_number = ?";
		Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceNumber));
		return Duration.between(now, cutoff).toSeconds();
	}

	public long getTimeLeftToGuessYear() {
		Instant now = Instant.now();
		final String getCutoff = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		Cutoff cutoff = new Cutoff();
		Instant cutoffYear = Instant
				.parse(jdbcTemplate.queryForObject(getCutoff, String.class, cutoff.getCurrentYear()));
		return Duration.between(now, cutoffYear).toSeconds();
	}

	public List<String> getDriversFromStartingGrid(int raceNumber) {
		final String getDriversFromGrid = "SELECT driver FROM StartingGrid WHERE race_number = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getDriversFromGrid, String.class, raceNumber);
	}

	public String getGuessedDriverPlace(int raceNumber, String category) {
		final String getPreviousGuessSql = "SELECT driver FROM DriverPlaceGuess WHERE race_number = ? AND category = ?";
		return jdbcTemplate.queryForObject(getPreviousGuessSql, String.class, raceNumber, category);
	}

	public void addDriverPlaceGuess(UUID id, int raceNumber, String driver, String category) {
		final String insertGuessSql = "REPLACE INTO DriverPlaceGuess (guesser, race_number, driver, category) values (?, ?, ?, ?)";
		jdbcTemplate.update(insertGuessSql, id, raceNumber, driver, category);
	}

	public List<String> getCompetitorsGuess(String competitorType, UUID id, int year) {
		if (competitorType.equals("driver")) {
			return getDriversGuess(id, year);
		}
		if (competitorType.equals("constructor")) {
			return getConstructorsGuess(id, year);
		}
		throw new IllegalArgumentException();
	}

	private List<String> getDriversGuess(UUID id, int year) {
		final String getGuessedSql = "SELECT driver FROM DriverGuess WHERE guesser = ? ORDER BY position ASC";
		final String getDriversSql = "SELECT driver FROM DriverYear WHERE year = ? ORDER BY position ASC";
		return getCompetitorGuess(id, year, getGuessedSql, getDriversSql);
	}

	private List<String> getConstructorsGuess(UUID id, int year) {
		final String getGuessedSql = "SELECT constructor FROM ConstructorGuess WHERE guesser = ? ORDER BY position ASC";
		final String getConstructorsSql = "SELECT constructor FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		return getCompetitorGuess(id, year, getGuessedSql, getConstructorsSql);
	}

	private List<String> getCompetitorGuess(UUID id, int year, final String getGuessedSql,
			final String getCompetitorsSql) {
		List<String> competitors = jdbcTemplate.queryForList(getGuessedSql, String.class, id);
		if (competitors.size() == 0) {
			return jdbcTemplate.queryForList(getCompetitorsSql, String.class, year);
		}
		return competitors;
	}

	public List<String> getCompetitorsYear(int year, String competitorType) {
		if (competitorType.equals("driver")) {
			return getDriversYear(year);
		} else if (competitorType.equals("constructor")) {
			return getConstructorsYear(year);
		}
		throw new IllegalArgumentException();
	}

	private List<String> getDriversYear(int year) {
		final String getDriversSql = "SELECT driver FROM DriverYear WHERE year = ?";
		return jdbcTemplate.queryForList(getDriversSql, String.class, year);
	}

	private List<String> getConstructorsYear(int year) {
		final String getConstructorSql = "SELECT constructor FROM ConstructorYear WHERE year = ?";
		return jdbcTemplate.queryForList(getConstructorSql, String.class, year);
	}

	public void insertCompetitorsYearGuess(String competitorType, UUID id, String competitor, int year, int position) {
		if (competitorType.equals("driver")) {
			insertDriversYearGuess(id, competitor, year, position);
			return;
		}
		if (competitorType.equals("constructor")) {
			insertConstructorsYearGuess(id, competitor, year, position);
			return;
		}
		throw new IllegalArgumentException();
	}

	private void insertDriversYearGuess(UUID id, String competitor, int year, int position) {
		final String addRowDriver = "REPLACE INTO DriverGuess (guesser, driver, year, position) values (?, ?, ?, ?)";
		jdbcTemplate.update(addRowDriver, id, competitor, year, position);
	}

	private void insertConstructorsYearGuess(UUID id, String competitor, int year, int position) {
		final String addRowConstructor = "REPLACE INTO ConstructorGuess (guesser, constructor, year, position) values (?, ?, ?, ?)";
		jdbcTemplate.update(addRowConstructor, id, competitor, year, position);
	}

	public List<String> getCategories() {
		final String sql = """
				SELECT name
				FROM Category
				""";
		return jdbcTemplate.queryForList(sql, String.class);
	}

	public List<Map<String, Object>> getPointsDiffMap(int year, String category) {
		final String sql = """
			SELECT points, diff
			FROM DiffPointsMap
			WHERE year = ? AND category = ?
			ORDER BY diff ASC
			""";

		return jdbcTemplate.queryForList(sql, year, category);
	}

	public List<UUID> getAllUsers() {
		final String getAllUsersSql = "SELECT id FROM User";
		return jdbcTemplate.queryForList(getAllUsersSql, UUID.class);
	}

	public List<String> getSeasonGuessers(int year) {
		final String getGussers = """
			SELECT DISTINCT u.username as username
			FROM User u
			JOIN FlagGuess fg ON fg.guesser = u.id
			JOIN DriverGuess dg ON dg.guesser = u.id
			JOIN ConstructorGuess cg ON cg.guesser = u.id
			WHERE fg.year = ? AND dg.year = ? AND cg.year = ?
			ORDER BY u.username ASC
			""";
		
		return jdbcTemplate.queryForList(getGussers, String.class, year, year, year);
	}

	public List<UUID> getSeasonGuesserIds(int year) {
		final String getGussers = """
			SELECT DISTINCT u.id as id
			FROM User u
			JOIN FlagGuess fg ON fg.guesser = u.id
			JOIN DriverGuess dg ON dg.guesser = u.id
			JOIN ConstructorGuess cg ON cg.guesser = u.id
			WHERE fg.year = ? AND dg.year = ? AND cg.year = ?
			ORDER BY u.username ASC
			""";
		
		return jdbcTemplate.queryForList(getGussers, UUID.class, year, year, year);
	}

	public List<Integer> getRaceIdsFinished(int year) {
		final String getRaceIds = """
			SELECT ro.id
			FROM RaceOrder ro
			JOIN Sprint s ON ro.id = s.race_number
			WHERE ro.year = ?
			ORDER BY ro.position ASC
			""";
		return jdbcTemplate.queryForList(getRaceIds, Integer.class, year);
	}

}
