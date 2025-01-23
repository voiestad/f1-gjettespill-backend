package no.vebb.f1.database;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.CutoffRace;
import no.vebb.f1.util.Flags;
import no.vebb.f1.user.User;
import no.vebb.f1.util.NoAvailableRaceException;
import no.vebb.f1.util.PositionedCompetitor;
import no.vebb.f1.util.RegisteredFlag;
import no.vebb.f1.util.TimeUtil;

@Service
@SuppressWarnings("null")
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
		Instant cutoffYear = Instant
				.parse(jdbcTemplate.queryForObject(getCutoff, String.class, TimeUtil.getCurrentYear()));
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

	public List<String> getDriversYear(int year) {
		final String getDriversSql = "SELECT driver FROM DriverYear WHERE year = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getDriversSql, String.class, year);
	}

	public List<String> getConstructorsYear(int year) {
		final String getConstructorSql = "SELECT constructor FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
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

	public boolean isValidCategory(String category) {
		final String validateCategory = "SELECT COUNT(*) FROM Category WHERE name = ?";
		return jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
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

	public int getMaxDiffInPointsMap(int year, String category) {
		final String getMaxDiff = "SELECT MAX(diff) FROM DiffPointsMap WHERE year = ? AND category = ?";
		return jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year, category);
	}

	public void addDiffToPointsMap(String category, int diff, int year) {
		final String addDiff = "INSERT INTO DiffPointsMap (category, diff, points, year) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(addDiff, category, diff, 0, year);
	}
	
	public void removeDiffToPointsMap(String category, int diff, int year) {
		final String deleteRowWithDiff = "DELETE FROM DiffPointsMap WHERE year = ? AND category = ? AND diff = ?";
		jdbcTemplate.update(deleteRowWithDiff, year, category, diff);
	}

	public void setNewDiffInPointsMap(String category, int diff, int year, int points) {
		final String setNewPoints = """
			UPDATE DiffPointsMap
			SET points = ?
			WHERE diff = ? AND year = ? AND category = ?
			""";
		jdbcTemplate.update(setNewPoints, points, diff, year, category);
	}

	public boolean isValidDiffInPointsMap(String category, int diff, int year) {
		final String validateDiff = "SELECT COUNT(*) FROM DiffPointsMap WHERE year = ? AND category = ? AND diff = ?";
		return jdbcTemplate.queryForObject(validateDiff, Integer.class, year, category, diff) > 0;
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

	public int getMaxRaceId(int year) {
		final String sql = """
			SELECT DISTINCT ro.id
			FROM Sprint s
			JOIN RaceOrder ro ON ro.id = s.race_number
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1
			""";
		return jdbcTemplate.queryForObject(sql, Integer.class, year);
	}

	public List<Map<String, Object>> getActiveRaces() {
		final String sql = """
			SELECT id, year, position
			FROM RaceOrder
			WHERE id NOT IN (SELECT race_number FROM RaceResult)
			ORDER BY year ASC, position ASC
			""";
		return jdbcTemplate.queryForList(sql);
	}

	public int getLatestStartingGridRaceId(int year) {
		final String getStartingGridId = """
			SELECT DISTINCT ro.id
			FROM StartingGrid sg
			JOIN RaceOrder ro on ro.id = sg.race_number
			AND ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1
			""";
		return jdbcTemplate.queryForObject(getStartingGridId, Integer.class, year);
	}

	public int getLatestRaceResultId(int year) {
		final String getRaceResultId = """
			SELECT ro.id
			FROM RaceResult rr
			JOIN RaceOrder ro on ro.id = rr.race_number
			AND ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1
			""";
		return jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year);
	}

	public int getRaceIdForSprint(int year) {
		final String getRaceResultId = """
			SELECT ro.id
			FROM RaceOrder ro
			WHERE ro.year = ?
			AND ro.id NOT IN (SELECT race_number FROM RaceResult)
			ORDER BY ro.position ASC
			LIMIT 1
			""";
		return jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year);
	}

	public boolean isStartingGridAdded(int raceNumber) {
		final String existCheck = "SELECT COUNT(*) FROM StartingGrid WHERE race_number = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceNumber) > 0;
	}

	public boolean isRaceResultAdded(int raceNumber) {
		final String existCheck = "SELECT COUNT(*) FROM RaceResult WHERE race_number = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceNumber) > 0;
	}

	public boolean isSprintAdded(int raceNumber) {
		final String existCheck = "SELECT COUNT(*) FROM Sprint WHERE race_number = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceNumber) > 0;
	}

	public boolean isRaceAdded(int raceNumber) {
		final String existCheck = "SELECT COUNT(*) FROM Race WHERE id = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceNumber) > 0;
	}

	public void addDriver(String driver) {
		final String insertDriver = "INSERT OR IGNORE INTO Driver (name) VALUES (?)";
		jdbcTemplate.update(insertDriver, driver);
	}

	public void addDriverYear(String driver, int year) {
		addDriver(driver);
		int position = getMaxPosDriverYear(year) + 1;
		addDriverYear(driver, year, position);
	}

	public int getMaxPosDriverYear(int year) {
		final String getMaxPos = "SELECT COALESCE(MAX(position), 0) FROM DriverYear WHERE year = ?";
		return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year);
	}

	public void addDriverYear(String driver, int year, int position) {
		final String addDriverYear = "INSERT INTO DriverYear (driver, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(addDriverYear, driver, year, position);
	}

	public void deleteDriverYear(String driver, int year) {
		final String deleteDriver = "DELETE FROM DriverYear WHERE year = ? AND driver = ?";
		jdbcTemplate.update(deleteDriver, year, driver);
	}

	public void deleteAllDriverYear(int year) {
		final String deleteAllDrivers = "DELETE FROM DriverYear WHERE year = ?";
		jdbcTemplate.update(deleteAllDrivers, year);
	}
	
	public void addConstructor(String constructor) {
		final String insertConstructor = "INSERT OR IGNORE INTO Constructor (name) VALUES (?)";
		jdbcTemplate.update(insertConstructor, constructor);
	}
	
	public void addConstructorYear(String constructor, int year) {
		addConstructor(constructor);
		int position = getMaxPosConstructorYear(year) + 1;
		addConstructorYear(constructor, year, position);
	}

	public int getMaxPosConstructorYear(int year) {
		final String getMaxPos = "SELECT COALESCE(MAX(position), 0) FROM ConstructorYear WHERE year = ?";
		return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year);
	}

	public void addConstructorYear(String constructor, int year, int position) {
		final String addConstructorYear = "INSERT INTO ConstructorYear (constructor, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(addConstructorYear, constructor, year, position);
	}

	public void deleteConstructorYear(String constructor, int year) {
		final String deleteConstructor = "DELETE FROM ConstructorYear WHERE year = ? AND constructor = ?";
		jdbcTemplate.update(deleteConstructor, year, constructor);
	}

	public void deleteAllConstructorYear(int year) {
		final String deleteAllConstructors = "DELETE FROM ConstructorYear WHERE year = ?";
		jdbcTemplate.update(deleteAllConstructors, year);
	}

	public void insertDriverStartingGrid(int raceNumber, int position, String driver) {
		final String insertStartingGrid = "INSERT OR REPLACE INTO StartingGrid (race_number, position, driver) VALUES (?, ?, ?)";
		jdbcTemplate.update(insertStartingGrid, raceNumber, position, driver);
	}

	public void addSprint(int raceNumber) {
		final String insertSprint = "INSERT OR IGNORE INTO Sprint VALUES (?)";
		jdbcTemplate.update(insertSprint, raceNumber);
	}

	public void insertDriverRaceResult(int raceNumber, String position, String driver, int points, int finishingPosition) {
		final String insertRaceResult = "INSERT OR REPLACE INTO RaceResult (race_number, position, driver, points, finishing_position) VALUES (?, ?, ?, ?, ?)";
		jdbcTemplate.update(insertRaceResult, raceNumber, position, driver, points, finishingPosition);
	}

	public void insertDriverIntoStandings(int race, String driver, int position, int points) {
		final String insertDriverStandings = "INSERT OR REPLACE INTO DriverStandings (race_number, driver, position, points) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(insertDriverStandings, race, driver, position, points);
	}
	
	public void insertConstructorIntoStandings(int race, String constructor, int position, int points) {
		final String insertConstructorStandings = "INSERT OR REPLACE INTO ConstructorStandings (race_number, constructor, position, points) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(insertConstructorStandings, race, constructor, position, points);
	}

	public void insertRace(int raceNumber, String raceName) {
		final String insertRaceName = "INSERT OR IGNORE INTO Race (id, name) VALUES (?, ?)";
		jdbcTemplate.update(insertRaceName, raceNumber, raceName);
	}

	public int getMaxRaceOrderPosition(int year) {
		final String sql = "SELECT MAX(position) FROM RaceOrder WHERE year = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, year);
	}

	public void insertRaceOrder(int raceNumber, int year, int position) {
		final String insertRaceOrder = "INSERT OR IGNORE INTO RaceOrder (id, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(insertRaceOrder, raceNumber, year, position);
	}

	public void deleteRace(int raceId) {
		final String deleteRace = "DELETE FROM Race WHERE id = ?";
		jdbcTemplate.update(deleteRace, raceId);
	}

	public boolean isValidSeason(int year) {
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		return jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
	}

	public boolean isValidRaceInSeason(int id, int year) {
		final String validateRaceId = "SELECT COUNT(*) FROM RaceOrder WHERE year = ? AND id = ?";
		return jdbcTemplate.queryForObject(validateRaceId, Integer.class, year, id) > 0;
	}

	public List<Integer> getAllValidYears() {
		final String sql = "SELECT DISTINCT year FROM RaceOrder ORDER BY year DESC";
		return jdbcTemplate.queryForList(sql, Integer.class);
	}

	public List<Integer> getRacesFromSeason(int year) {
		final String getRaceIds = "SELECT id FROM RaceOrder WHERE year = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getRaceIds, Integer.class, year);
	}

	public void removeRaceOrderFromSeason(int year) {
		final String removeOldOrderSql = "DELETE FROM RaceOrder WHERE year = ?";
		jdbcTemplate.update(removeOldOrderSql, year);
	}

	public List<CutoffRace> getCutoffRaces(int year) {
		List<CutoffRace> races = new ArrayList<>();
		final String getCutoffRaces = """
						SELECT r.id as id, r.name as name, rc.cutoff as cutoff, ro.year as year, ro.position as position
						FROM RaceCutoff rc
						JOIN RaceOrder ro ON ro.id = rc.race_number
						JOIN Race r ON ro.id = r.id
						WHERE ro.year = ?
						ORDER BY ro.position ASC
				""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getCutoffRaces, year);
		for (Map<String, Object> row : sqlRes) {
			LocalDateTime cutoff = TimeUtil.instantToLocalTime(Instant.parse((String) row.get("cutoff")));
			String name = (String) row.get("name");
			int id = (int) row.get("id");
			int position = (int) row.get("position");
			CutoffRace race = new CutoffRace(position, name, id, cutoff);
			races.add(race);
		}
		return races;
	}

	public LocalDateTime getCutoffYearLocalTime(int year) {
		return TimeUtil.instantToLocalTime(getCutoffYear(year));
	}

	public void setCutoffRace(Instant cutoffTime, int raceId) {
		final String setCutoffTime = "INSERT OR REPLACE INTO RaceCutoff (race_number, cutoff) VALUES (?, ?)";
		jdbcTemplate.update(setCutoffTime, raceId, cutoffTime.toString());
	}

	public void setCutoffYear(Instant cutoffTime, int year) {
		final String setCutoffTime = "INSERT OR REPLACE INTO YearCutoff (year, cutoff) VALUES (?, ?)";
		jdbcTemplate.update(setCutoffTime, year, cutoffTime.toString());
	}

	public List<RegisteredFlag> getRegisteredFlags(int raceId) {
		List<RegisteredFlag> registeredFlags = new ArrayList<>();
		final String getRegisteredFlags = "SELECT flag, round, id FROM FlagStats WHERE race_number = ?";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRegisteredFlags, raceId);
		for (Map<String, Object> row : sqlRes) {
			String type = (String) row.get("flag");
			int round = (int) row.get("round");
			int id = (int) row.get("id");

			registeredFlags.add(new RegisteredFlag(type, round, id));
		}
		return registeredFlags;
	}

	public void insertFlagStats(String flag, int round, int raceId) {
		final String sql = "INSERT INTO FlagStats (flag, race_number, round) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, flag, raceId, round);
	}

	public void deleteFlagStatsById(int id) {
		final String sql = "DELETE FROM FlagStats WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}

	public String getRaceName(int raceId) {
		final String getRaceNameSql = "SELECT name FROM Race WHERE id = ?";
		return jdbcTemplate.queryForObject(getRaceNameSql, String.class, raceId);
	}

	public List<String> getFlags() {
		final String sql = "SELECT name FROM Flag";
		return jdbcTemplate.queryForList(sql, String.class);
	}

	public List<PositionedCompetitor> getStartingGrid(int raceId) {
		final String getStartingGrid = """
				SELECT position, driver
				FROM StartingGrid
				WHERE race_number = ?
				ORDER BY position ASC
				""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getStartingGrid, raceId);
		List<PositionedCompetitor> startingGrid = new ArrayList<>();
		for (Map<String, Object> row : sqlRes) {
			String position = String.valueOf((int) row.get("position"));
			String driver = (String) row.get("driver");
			startingGrid.add(new PositionedCompetitor(position, driver, ""));
		}
		return startingGrid;
	}

	public List<PositionedCompetitor> getRaceResult(int raceId) {
		final String getRaceResult = """
			SELECT position, driver, points
			FROM RaceResult
			WHERE race_number = ?
			ORDER BY finishing_position ASC
			""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRaceResult, raceId);
		List<PositionedCompetitor> raceResult = new ArrayList<>();
		for (Map<String, Object> row : sqlRes) {
			String position = (String) row.get("position");
			String driver = (String) row.get("driver");
			String points = (String) row.get("points");
			raceResult.add(new PositionedCompetitor(position, driver, points));
		}
		return raceResult;
	}

	public List<PositionedCompetitor> getDriverStandings(int raceId) {
		final String getDriverStandings = """
			SELECT position, driver, points
			FROM DriverStandings
			WHERE race_number = ?
			ORDER BY position ASC
			""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getDriverStandings, raceId);
		List<PositionedCompetitor> standings = new ArrayList<>();
		for (Map<String, Object> row : sqlRes) {
			String position = String.valueOf((int) row.get("position"));
			String driver = (String) row.get("driver");
			String points = (String) row.get("points");;
			standings.add(new PositionedCompetitor(position, driver, points));
		}
		return standings;
	}
	
	public List<PositionedCompetitor> getConstructorStandings(int raceId) {
		final String getConstructorStandings = """
			SELECT position, constructor, points
			FROM ConstructorStandings
			WHERE race_number = ?
			ORDER BY position ASC
			""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getConstructorStandings, raceId);
		List<PositionedCompetitor> standings = new ArrayList<>();
		for (Map<String, Object> row : sqlRes) {
			String position = String.valueOf((int) row.get("position"));
			String constructor = (String) row.get("constructor");
			String points = (String) row.get("points");;
			standings.add(new PositionedCompetitor(position, constructor, points));
		}
		return standings;
	}

	public boolean isValidDriverYear(String driver, int year) {
		final String existCheck = "SELECT COUNT(*) FROM DriverYear WHERE year = ? AND driver = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, year, driver) > 0;
	}
	
	public boolean isValidConstructorYear(String constructor, int year) {
		final String existCheck = "SELECT COUNT(*) FROM ConstructorYear WHERE year = ? AND constructor = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, year, constructor) > 0;
	}
}
