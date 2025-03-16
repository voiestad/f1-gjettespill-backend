package no.vebb.f1.database;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.*;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.Flags;
import no.vebb.f1.util.collection.PositionedCompetitor;
import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.collection.UserRaceGuess;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Color;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.MailOption;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserMail;

@Service
@SuppressWarnings("null")
public class Database {

	private JdbcTemplate jdbcTemplate;

	public Database(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Gets the cutoff for guessing on categories that happens before the season 
	 * starts.
	 * 
	 * @param year of season
	 * @return time as Instant
	 * @throws EmptyResultDataAccessException if year cutoff does not exist
	 */
	public Instant getCutoffYear(Year year) throws EmptyResultDataAccessException {
		final String getCutoff = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, year));
	}

	/**
	 * Gets the cutoff for guessing on race specific categories.
	 * 
	 * @param raceId of race
	 * @return time as Instant
	 * @throws NoAvailableRaceException if race does not have a cutoff set
	 */
	public Instant getCutoffRace(RaceId raceId) throws NoAvailableRaceException {
		try {
			final String getCutoff = "SELECT cutoff FROM RaceCutoff WHERE race_number = ?";
			return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceId));
		} catch (EmptyResultDataAccessException e) {
			throw new NoAvailableRaceException("There is no cutoff for the given raceId '" + raceId + "'");
		}
	}

	/**
	 * Checks if the user is admin based on given id.
	 * 
	 * @param userId to check
	 * @return true if user is admin
	 */
	public boolean isUserAdmin(UUID userId) {
		final String sql = "SELECT COUNT(*) FROM Admin WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
	}

	/**
	 * Gets the User object for the given id.
	 * 
	 * @param userId for user to get
	 * @return User
	 * @throws EmptyResultDataAccessException if user not found in database
	 */
	public User getUserFromId(UUID userId) throws EmptyResultDataAccessException {
		final String sql = "SELECT username, google_id FROM User WHERE id = ?";
		Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, userId);
		String username = (String) sqlRes.get("username");
		String googleId = (String) sqlRes.get("google_id");
		return new User(googleId, userId, username);
	}

	/**
	 * Gets the User object for the given googleId.
	 * 
	 * @param googleId as String
	 * @return User
	 * @throws EmptyResultDataAccessException if user not found in database
	 */
	public User getUserFromGoogleId(String googleId) throws EmptyResultDataAccessException {
		final String sql = "SELECT username, id FROM User WHERE google_id = ?";
		Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, googleId);
		String username = (String) sqlRes.get("username");
		UUID id = UUID.fromString((String) sqlRes.get("id"));
		return new User(googleId, id, username);
	}

	/**
	 * Gets the latest race number that has either had a race.
	 * 
	 * @param year of season
	 * @return race number of race
	 */
	public RaceId getLatestRaceId(Year year) throws EmptyResultDataAccessException {
		final String getRaceIdSql = """
			SELECT ro.id
			FROM RaceOrder ro
			JOIN RaceResult rr ON ro.id = rr.race_number
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1;
			""";

		return new RaceId(jdbcTemplate.queryForObject(getRaceIdSql, Integer.class, year));
	}

	/**
	 * Gets the position of a race within the season it is in.
	 * 
	 * @param raceId of race
	 * @return position of race
	 */
	public int getPositionOfRace(RaceId raceId) {
		final String getRacePosition = "SELECT position FROM RaceOrder WHERE id = ?";
		return jdbcTemplate.queryForObject(getRacePosition, Integer.class, raceId);
	}

	/**
	 * Translates a category from its backend representation to its display string.
	 * 
	 * @param category to translate
	 * @return translation of category
	 */
	public String translateCategory(Category category) {
		final String translationSql = """
			SELECT translation
			FROM CategoryTranslation
			WHERE category = ?
			""";

		return jdbcTemplate.queryForObject(translationSql, String.class, category);
	}

	/**
	 * Translates a flag from its backend representation to its display string.
	 * 
	 * @param flag to translate
	 * @return translation of flag
	 */
	public String translateFlagName(Flag flag) {
		final String translationSql = """
			SELECT translation
			FROM FlagTranslation
			WHERE flag = ?
			""";

		return jdbcTemplate.queryForObject(translationSql, String.class, flag);
	}

	/**
	 * Gets the data for a users guesses on flags up until the given race position
	 * of the given year. If race position is 0, actual amount will also be 0.
	 * 
	 * @param racePos position within a season
	 * @param year of season
	 * @param userId of guesser/user
	 * @return "table" of guesses
	 */
	public List<Map<String, Object>> getDataForFlagTable(int racePos, Year year, UUID userId) {
		if (racePos == 0) {
			final String sqlNoRace = """
				SELECT f.name AS type, fg.amount AS guessed, 0 AS actual
				FROM Flag f
				JOIN FlagGuess fg ON f.name = fg.flag
				JOIN RaceOrder ro ON fg.year = ro.year
				WHERE ro.year = ? AND fg.guesser = ?
				GROUP BY f.name
				""";
			return jdbcTemplate.queryForList(sqlNoRace, year, userId);
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
			return jdbcTemplate.queryForList(sql, year, userId, racePos);
		}
	}

	/**
	 * Gets the data for a users guesses on the given race of the given year.
	 * Columns: race_position, race_name, driver, start, finish
	 * 
	 * @param category to get table for
	 * @param userId of guesser/user
	 * @param year of season
	 * @param racePos position within a season
	 * @return "table" of guesses
	 */
	public List<Map<String, Object>> getDataForPlaceGuessTable(Category category, UUID userId, Year year, int racePos) {
		final String sql = """
			SELECT ro.position as race_position, r.name AS race_name, dpg.driver AS driver, sg.position AS start, rr.finishing_position AS finish
			FROM DriverPlaceGuess dpg
			JOIN Race r ON r.id = dpg.race_number
			JOIN RaceOrder ro ON r.id = ro.id
			JOIN StartingGrid sg ON sg.race_number = r.id AND dpg.driver = sg.driver
			JOIN RaceResult rr ON rr.race_number = r.id AND dpg.driver = rr.driver
			WHERE dpg.category = ? AND dpg.guesser = ? AND ro.year = ? AND ro.position <= ?
			ORDER BY ro.position ASC
			""";
		return jdbcTemplate.queryForList(sql, category, userId, year, racePos);
	}

	/**
	 * Gets a list of drivers guessed by the given user in the given year.
	 * Ordered by position of guesses in ascending order.
	 * 
	 * @param year of season
	 * @param userId of user
	 * @return drivers ascendingly
	 */
	public List<Driver> getGuessedYearDriver(Year year, UUID userId) {
		final String guessedSql = "SELECT driver FROM DriverGuess WHERE year = ?  AND guesser = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(guessedSql, String.class, year, userId).stream()
			.map(driver -> new Driver(driver))
			.toList();
	}

	/**
	 * Gets a list of constructors guessed by the given user in the given year.
	 * Ordered by position of guesses in ascending order.
	 * 
	 * @param year of season
	 * @param userId of user
	 * @return constructors ascendingly
	 */
	public List<Constructor> getGuessedYearConstructor(Year year, UUID userId) {
		final String guessedSql = "SELECT constructor FROM ConstructorGuess WHERE year = ? AND guesser = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(guessedSql, String.class, year, userId).stream()
			.map(constructor -> new Constructor(constructor))
			.toList();
	}

	/**
	 * Gets the driver standings for a given race and year.
	 * If the race id is set to -1, the position set in the DriverYear will be used as
	 * default order.
	 * Ordered by position of standings in ascending order.
	 * 
	 * @param raceId of race
	 * @param year of season
	 * @return drivers ascendingly
	 */
	public List<Driver> getDriverStandings(RaceId raceId, Year year) {
		final String driverYearSql = "SELECT driver FROM DriverYear WHERE year = ? ORDER BY position ASC";
		final String driverStandingsSql = "SELECT driver FROM DriverStandings WHERE race_number = ? ORDER BY position ASC";
		List<String> result;
		if (raceId == null) {
			result = jdbcTemplate.queryForList(driverYearSql, String.class, year);
		} else {
			result =jdbcTemplate.queryForList(driverStandingsSql, String.class, raceId);
		}
		return result.stream().map(driver -> new Driver(driver)).toList();
	}

	/**
	 * Gets the constructor standings for a given race and year.
	 * If the race number is set to -1, the position set in the ConstructorYear will be used as
	 * default order.
	 * Ordered by position of standings in ascending order.
	 * 
	 * @param raceId of race
	 * @param year of season
	 * @return constructors ascendingly
	 */
	public List<Constructor> getConstructorStandings(RaceId raceId, Year year) {
		final String constructorYearSql = "SELECT constructor FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		final String constructorStandingsSql = "SELECT constructor FROM ConstructorStandings WHERE race_number = ? ORDER BY position ASC";
		List<String> result;
		if (raceId == null) {
			result = jdbcTemplate.queryForList(constructorYearSql, String.class, year);
		} else {
			result =jdbcTemplate.queryForList(constructorStandingsSql, String.class, raceId);
		}
		return result.stream().map(constructor -> new Constructor(constructor)).toList();
	}

	/**
	 * Checks if the username is already in use by a user.
	 * NOTE: Username should be in uppercase.
	 * 
	 * @param usernameUpper the username in uppercase
	 * @return true if username is in use
	 */
	public boolean isUsernameInUse(String usernameUpper) {
		final String sqlCheckUsername = "SELECT COUNT(*) FROM User WHERE username_upper = ?";
		return jdbcTemplate.queryForObject(sqlCheckUsername, Integer.class, usernameUpper) > 0;
	}

	/**
	 * Updates the username of the given user to the given username.
	 * 
	 * @param username to set as new username
	 * @param userId of user
	 */
	public void updateUsername(Username username, UUID userId) {
		final String updateUsername = """
			UPDATE User
			SET username = ?, username_upper = ?
			WHERE id = ?
			""";
		jdbcTemplate.update(updateUsername, username.username, username.usernameUpper, userId);
	}

	/**
	 * Deletes the account of the given user.
	 * Sets the username to 'Anonym' and google_id to id.
	 * 
	 * @param userId of user
	 */
	public void deleteUser(UUID userId) {
		final String deleteUser = """
			UPDATE User
			SET username = 'Anonym', username_upper = 'ANONYM', google_id = ?
			WHERE id = ?
			""";
		clearUserFromMailing(userId);
		jdbcTemplate.update(deleteUser, userId, userId);
	}

	/**
	 * Adds a user with the given username and google ID to the database.
	 * Sets a random UUID as the users ID.
	 * 
	 * @param username
	 * @param googleId the ID provided by OAUTH
	 */
	public void addUser(Username username, String googleId) {
		final String sqlInsertUsername = "INSERT INTO User (google_id, id,username, username_upper) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(sqlInsertUsername, googleId, UUID.randomUUID(), username.username, username.usernameUpper);
	}

	/**
	 * Gets the guesses of all users for the given race in the given category.
	 * 
	 * @param raceId of race
	 * @param category
	 * @return list of guesses
	 */
	public List<UserRaceGuess> getUserGuessesDriverPlace(RaceId raceId, Category category) {
		final String getGuessSql = """
			SELECT u.username AS username, dpg.driver AS driver, sg.position AS position
			FROM DriverPlaceGuess dpg
			JOIN User u ON u.id = dpg.guesser
			JOIN StartingGrid sg ON sg.race_number = dpg.race_number AND sg.driver = dpg.driver
			WHERE dpg.race_number = ? AND dpg.category = ?
			ORDER BY u.username ASC
			""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getGuessSql, raceId, category);
		return sqlRes.stream()
			.map(row -> new UserRaceGuess(
				(String) row.get("username"),
				(String) row.get("driver"),
				(int) row.get("position")))
			.toList();
	}

	/**
	 * Gets the latest race which has a starting grid available in the given year.
	 * 
	 * @param year of season
	 * @return race
	 * @throws EmptyResultDataAccessException if there is no race within the criteria
	 */
	public CutoffRace getLatestRaceForPlaceGuess(Year year) throws EmptyResultDataAccessException {
		final String getRaceIdSql = """
			SELECT ro.id AS id, ro.position AS position, r.name AS name
			FROM RaceOrder ro
			JOIN StartingGrid sg ON ro.id = sg.race_number
			JOIN Race r ON r.id = ro.id
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1;
			""";
		Map<String, Object> res = jdbcTemplate.queryForMap(getRaceIdSql, year);
		RaceId raceId = new RaceId((int) res.get("id"));
		return new CutoffRace((int) res.get("position"), (String) res.get("name"), raceId);
	}

	/**
	 * Gets the current race to guess on. Only returns a race if there is a race
	 * that has a starting grid and not race result.
	 * 
	 * @return race id
	 * @throws EmptyResultDataAccessException if there is no race within the criteria
	 */
	public RaceId getCurrentRaceIdToGuess() throws EmptyResultDataAccessException {
		final String getRaceId = """
			SELECT DISTINCT race_number
			FROM StartingGrid sg
			WHERE sg.race_number NOT IN (
				SELECT rr.race_number
				FROM RaceResult rr
			)
			""";
		return new RaceId(jdbcTemplate.queryForObject(getRaceId, Integer.class));
	}

	/**
	 * Adds the guesses of flags of a user into the given year.
	 * Overwrites pre-existing guesses.
	 * 
	 * @param userId of user
	 * @param year of season
	 * @param flags the user guessed
	 */
	public void addFlagGuesses(UUID userId, Year year, Flags flags) {
		final String sql = "REPLACE INTO FlagGuess (guesser, flag, year, amount) values (?, ?, ?, ?)";
		jdbcTemplate.update(sql, userId, "Yellow Flag", year, flags.yellow);
		jdbcTemplate.update(sql, userId, "Red Flag", year, flags.red);
		jdbcTemplate.update(sql, userId, "Safety Car", year, flags.safetyCar);
	}

	/**
	 * Gets the flag guesses of the given user in the given year.
	 * 
	 * @param userId of user
	 * @param year of season
	 * @return flag guesses
	 */
	public Flags getFlagGuesses(UUID userId, Year year) {
		final String sql = "SELECT flag, amount FROM FlagGuess WHERE guesser = ? AND year = ?";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, userId, year);
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

	/**
	 * Gets number of seconds remaining to guess in the given race.
	 * 
	 * @param raceId of race
	 * @return time left in seconds
	 */
	public long getTimeLeftToGuessRace(RaceId raceId) {
		Instant now = Instant.now();
		final String getCutoff = "SELECT cutoff FROM RaceCutoff WHERE race_number = ?";
		Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceId));
		return Duration.between(now, cutoff).toSeconds();
	}

	/**
	 * Gets number of seconds remaining to guess in the year.
	 * 
	 * @return time left in seconds
	 */
	public long getTimeLeftToGuessYear() {
		Instant now = Instant.now();
		final String getCutoff = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		Instant cutoffYear = Instant
				.parse(jdbcTemplate.queryForObject(getCutoff, String.class, TimeUtil.getCurrentYear()));
		return Duration.between(now, cutoffYear).toSeconds();
	}

	/**
	 * Gets a list of starting grid from the given race.
	 * Drivers are ordered from first to last ascendingly.
	 * 
	 * @param raceId of race
	 * @return drivers ascendingly
	 */
	public List<Driver> getDriversFromStartingGrid(RaceId raceId) {
		final String getDriversFromGrid = "SELECT driver FROM StartingGrid WHERE race_number = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getDriversFromGrid, String.class, raceId).stream()
			.map(driver -> new Driver(driver))
			.toList();
	}

	public List<ColoredCompetitor<Driver>> getDriversFromStartingGridWithColors(RaceId raceId) {
		final String getDriversFromGrid = """
			SELECT sg.driver as driver, cc.color as color
			FROM StartingGrid sg
			LEFT JOIN DriverTeam dt ON dt.driver = sg.driver
			LEFT JOIN ConstructorColor cc ON cc.constructor = dt.team
			WHERE race_number = ?
			ORDER BY position ASC
			""";
		return jdbcTemplate.queryForList(getDriversFromGrid, raceId).stream()
			.map(row -> new ColoredCompetitor<>(new Driver((String) row.get("driver")), new Color((String) row.get("color"))))
			.toList();
	}

	/**
	 * Gets the previous guess of a user on driver place guess.
	 * 
	 * @param raceId of race
	 * @param category guessed on
	 * @param userId of the user
	 * @return name of driver guessed
	 */
	public Driver getGuessedDriverPlace(RaceId raceId, Category category, UUID userId) {
		final String getPreviousGuessSql = """
			SELECT driver
			FROM DriverPlaceGuess
			WHERE race_number = ? AND category = ? AND guesser = ?
			""";
		return new Driver(jdbcTemplate.queryForObject(getPreviousGuessSql, String.class, raceId, category, userId));
	}

	/**
	 * Adds driver place guess to the database.
	 * 
	 * @param userId of the guesser
	 * @param raceId of race
	 * @param driver name guessed
	 * @param category which the user guessed on
	 */
	public void addDriverPlaceGuess(UUID userId, RaceId raceId, Driver driver, Category category) {
		final String insertGuessSql = "REPLACE INTO DriverPlaceGuess (guesser, race_number, driver, category) values (?, ?, ?, ?)";
		jdbcTemplate.update(insertGuessSql, userId, raceId, driver, category);
	}

	/**
	 * Gets a list of a users guesses on a drivers in a given season.
	 * 
	 * @param userId of user
	 * @param year of season
	 * @return competitors ascendingly
	 */
	public List<ColoredCompetitor<Driver>> getDriversGuess(UUID userId, Year year) {
		final String getGuessedSql = """
			SELECT dg.driver as driver, cc.color as color
			FROM DriverGuess dg
			LEFT JOIN DriverTeam dt ON dt.driver = dg.driver
			LEFT JOIN ConstructorColor cc ON cc.constructor = dt.team
			WHERE dg.guesser = ?
			ORDER BY position ASC
			""";
		final String getDriversSql = """
			SELECT dy.driver as driver, cc.color as color
			FROM DriverYear dy
			LEFT JOIN DriverTeam dt ON dt.driver = dy.driver
			LEFT JOIN ConstructorColor cc ON cc.constructor = dt.team
			WHERE dy.year = ?
			ORDER BY position ASC
			""";
		return getCompetitorGuess(userId, year, getGuessedSql, getDriversSql).stream()
			.map(row -> new ColoredCompetitor<>(
				new Driver((String) row.get("driver")),
				new Color((String) row.get("color"))))
			.toList();
	}

	/**
	 * Gets a list of a users guesses on constructors in a given season.
	 * 
	 * @param userId of user
	 * @param year of season
	 * @return competitors ascendingly
	 */
	public List<ColoredCompetitor<Constructor>> getConstructorsGuess(UUID userId, Year year) {
		final String getGuessedSql = """
			SELECT cg.constructor as constructor, cc.color as color
			FROM ConstructorGuess cg
			LEFT JOIN ConstructorColor cc ON cc.constructor = cg.constructor
			WHERE cg.guesser = ?
			ORDER BY position ASC
			""";
		final String getConstructorsSql = """
			SELECT cy.constructor as constructor, cc.color as color
			FROM ConstructorYear cy
			LEFT JOIN ConstructorColor cc ON cc.constructor = cy.constructor
			WHERE cy.year = ?
			ORDER BY position ASC
			""";
		return getCompetitorGuess(userId, year, getGuessedSql, getConstructorsSql).stream()
			.map(row -> new ColoredCompetitor<>(
				new Constructor((String) row.get("constructor")),
				new Color((String) row.get("color"))))
			.toList();
	}

	private List<Map<String, Object>> getCompetitorGuess(UUID userId, Year year, final String getGuessedSql,
			final String getCompetitorsSql) {
		List<Map<String, Object>> competitors = jdbcTemplate.queryForList(getGuessedSql, userId);
		if (competitors.size() == 0) {
			return jdbcTemplate.queryForList(getCompetitorsSql, year);
		}
		return competitors;
	}

	/**
	 * Gets a list of a yearly drivers in a given season.
	 * 
	 * @param year of season
	 * @return drivers ascendingly
	 */
	public List<Driver> getDriversYear(Year year) {
		final String getDriversSql = "SELECT driver FROM DriverYear WHERE year = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getDriversSql, String.class, year).stream()
			.map(driver -> new Driver(driver))
			.toList();
	}

	/**
	 * Gets a list of a yearly constructors in a given season.
	 * 
	 * @param year of season
	 * @return constructors ascendingly
	 */
	public List<Constructor> getConstructorsYear(Year year) {
		final String getConstructorSql = "SELECT constructor FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getConstructorSql, String.class, year).stream()
		.map(constructor -> new Constructor(constructor))
		.toList();
	}

	/**
	 * Adds a guess for a user on the ranking of a driver.
	 * 
	 * @param userId of user
	 * @param driver name
	 * @param year of season
	 * @param position guessed
	 */
	public void insertDriversYearGuess(UUID userId, Driver driver, Year year, int position) {
		final String addRowDriver = "REPLACE INTO DriverGuess (guesser, driver, year, position) values (?, ?, ?, ?)";
		jdbcTemplate.update(addRowDriver, userId, driver, year, position);
	}

	/**
	 * Adds a guess for a user on the ranking of a constructor.
	 * 
	 * @param userId of user
	 * @param constructor name
	 * @param year of season
	 * @param position guessed
	 */
	public void insertConstructorsYearGuess(UUID userId, Constructor constructor, Year year, int position) {
		final String addRowConstructor = "REPLACE INTO ConstructorGuess (guesser, constructor, year, position) values (?, ?, ?, ?)";
		jdbcTemplate.update(addRowConstructor, userId, constructor, year, position);
	}

	/**
	 * Gets a list of all guessing categories.
	 * 
	 * @return categories 
	 */
	public List<Category> getCategories() {
		final String sql = """
			SELECT name
			FROM Category
			""";
		return jdbcTemplate.queryForList(sql, String.class).stream()
			.map(name -> new Category(name, this))
			.toList();
	}

	/**
	 * Checks if the given category is a valid category
	 * 
	 * @param category name
	 * @return true if category is valid
	 */
	public boolean isValidCategory(String category) {
		final String validateCategory = "SELECT COUNT(*) FROM Category WHERE name = ?";
		return jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
	}

	/**
	 * Gets a mapping from the difference of a guess to the points
	 * obtained by the difference in a given category.
	 * 
	 * @param category name
	 * @param year of season
	 * @return map from diff to points
	 */
	public Map<Diff, Points> getDiffPointsMap(Year year, Category category) {
		final String sql = """
			SELECT points, diff
			FROM DiffPointsMap
			WHERE year = ? AND category = ?
			ORDER BY diff ASC
			""";

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, year, category);
		Map<Diff, Points> map = new LinkedHashMap<>();
		for (Map<String, Object> entry : result) {
			Diff diff = new Diff((int) entry.get("diff"));
			Points points = new Points((int) entry.get("points"));
			map.put(diff, points);
		}
		return map;

	}

	/**
	 * Gets max diff in mapping from diff to points in the given season and category.
	 * 
	 * @param year of season
	 * @param category name
	 * @return max diff
	 */
	public Diff getMaxDiffInPointsMap(Year year, Category category) {
		final String getMaxDiff = "SELECT MAX(diff) FROM DiffPointsMap WHERE year = ? AND category = ?";
		return new Diff(jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year, category));
	}

	/**
	 * Adds a new mapping from the given diff to 0 points in the given season and category.
	 * 
	 * @param category name
	 * @param diff to add mapping for
	 * @param year of season
	 */
	public void addDiffToPointsMap(Category category, Diff diff, Year year) {
		final String addDiff = "INSERT INTO DiffPointsMap (category, diff, points, year) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(addDiff, category, diff, 0, year);
	}
	
	/**
	 * Removes the mapping from the given diff in the given season and category.
	 * 
	 * @param category name
	 * @param diff to remove mapping for
	 * @param year of season
	 */
	public void removeDiffToPointsMap(Category category, Diff diff, Year year) {
		final String deleteRowWithDiff = "DELETE FROM DiffPointsMap WHERE year = ? AND category = ? AND diff = ?";
		jdbcTemplate.update(deleteRowWithDiff, year, category, diff);
	}

	/**
	 * Sets a new mapping from the given diff to the given points in the given season and category.
	 * 
	 * @param category name
	 * @param diff to set new mapping for
	 * @param year of season
	 * @param points
	 */
	public void setNewDiffToPointsInPointsMap(Category category, Diff diff, Year year, Points points) {
		final String setNewPoints = """
			UPDATE DiffPointsMap
			SET points = ?
			WHERE diff = ? AND year = ? AND category = ?
			""";
		jdbcTemplate.update(setNewPoints, points, diff, year, category);
	}

	/**
	 * Checks if there is a diff set for the given season and category.
	 * 
	 * @param category name
	 * @param diff to check
	 * @param year of season
	 */
	public boolean isValidDiffInPointsMap(Category category, Diff diff, Year year) {
		final String validateDiff = "SELECT COUNT(*) FROM DiffPointsMap WHERE year = ? AND category = ? AND diff = ?";
		return jdbcTemplate.queryForObject(validateDiff, Integer.class, year, category, diff) > 0;
	}

	/**
	 * Gets a list of all the id of all users.
	 * 
	 * @return id of every user
	 */
	public List<UUID> getAllUserIds() {
		final String getAllUsersSql = "SELECT id FROM User";
		return jdbcTemplate.queryForList(getAllUsersSql, UUID.class);
	}

	/**
	 * Gets a list of all all users sorted by username_upper.
	 * 
	 * @return every user
	 */
	public List<User> getAllUsers() {
		final String getAllUsersSql = """
			SELECT id, username, google_id
			FROM User
			ORDER BY username_upper ASC
			""";
		return jdbcTemplate.queryForList(getAllUsersSql).stream()
			.map(row -> 
			new User(
				(String) row.get("google_id"),
				UUID.fromString((String) row.get("id")),
				(String) row.get("username"))
			).toList();
	}

	/**
	 * Gets a list of every person that has guessed in a season. To qualify they have to have guessed
	 * on flags, drivers and constructors.
	 * Ordered ascendingly by username
	 * 
	 * @param year of season
	 * @return names of guessers
	 */
	public List<String> getSeasonGuessers(Year year) {
		final String getGussers = """
			SELECT DISTINCT u.username as username, u.id as id
			FROM User u
			JOIN FlagGuess fg ON fg.guesser = u.id
			JOIN DriverGuess dg ON dg.guesser = u.id
			JOIN ConstructorGuess cg ON cg.guesser = u.id
			WHERE fg.year = ? AND dg.year = ? AND cg.year = ?
			ORDER BY u.username ASC
			""";
		
		return jdbcTemplate.queryForList(getGussers, year, year, year).stream()
			.map(row -> (String) row.get("username"))
			.toList();
	}

	/**
	 * Gets a list of every person that has guessed in a season. To qualify they have to have guessed
	 * on flags, drivers and constructors.
	 * Ordered ascendingly by username
	 * 
	 * @param year of season
	 * @return id of guessers
	 */
	public List<UUID> getSeasonGuesserIds(Year year) {
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

	/**
	 * Gets every race id from a year where there has been a a race.
	 * 
	 * @param year of season
	 * @return id of races
	 */
	public List<RaceId> getRaceIdsFinished(Year year) {
		final String getRaceIds = """
			SELECT DISTINCT ro.id
			FROM RaceOrder ro
			JOIN RaceResult rr ON ro.id = rr.race_number
			WHERE ro.year = ?
			ORDER BY ro.position ASC
			""";
		return jdbcTemplate.queryForList(getRaceIds, Integer.class, year).stream()
			.map(id -> new RaceId(id))
			.toList();
	}

	/**
	 * Gets the id, year and position of every race that does not have a
	 * race result.
	 * Ordered by year and then position, both ascendingly.
	 * 
	 * @return list of rows with id, year and position
	 */
	public List<CutoffRace> getActiveRaces() {
		final String sql = """
			SELECT id, year, position
			FROM RaceOrder
			WHERE id NOT IN (SELECT race_number FROM RaceResult)
			ORDER BY year ASC, position ASC
			""";
		
		return jdbcTemplate.queryForList(sql).stream()
			.map(row -> new CutoffRace(
				(int) row.get("position"),
				new RaceId((int) row.get("id")),
				new Year((int) row.get("year"))
			))
			.toList();
	}

	/**
	 * Gets the id of the latest starting grid of a season.
	 * 
	 * @param year of season
	 * @return race id
	 */
	public RaceId getLatestStartingGridRaceId(Year year) {
		final String getStartingGridId = """
			SELECT DISTINCT ro.id
			FROM StartingGrid sg
			JOIN RaceOrder ro on ro.id = sg.race_number
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1
			""";
		return new RaceId(jdbcTemplate.queryForObject(getStartingGridId, Integer.class, year));
	}

	/**
	 * Gets the id of the latest race result of a season.
	 * 
	 * @param year of season
	 * @return race id
	 */
	public RaceId getLatestRaceResultId(Year year) {
		final String getRaceResultId = """
			SELECT ro.id
			FROM RaceResult rr
			JOIN RaceOrder ro on ro.id = rr.race_number
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1
			""";
		return new RaceId(jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year));
	}

	/**
	 * Gets the id of the latest race result of a season.
	 * 
	 * @param year of season
	 * @return race id
	 */
	public RaceId getLatestStandingsId(Year year) {
		final String getRaceResultId = """
			SELECT ro.id
			FROM RaceOrder ro
			JOIN DriverStandings ds on ds.race_number = ro.id
			JOIN ConstructorStandings cs on cs.race_number = ro.id
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1
			""";
		return new RaceId(jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year));
	}

	/**
	 * Checks if starting grid for race already exists.
	 * 
	 * @param raceId
	 * @return true if exists
	 */
	public boolean isStartingGridAdded(RaceId raceId) {
		final String existCheck = "SELECT COUNT(*) FROM StartingGrid WHERE race_number = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
	}

	/**
	 * Checks if race result for race already exists.
	 * 
	 * @param raceId
	 * @return true if exists
	 */
	public boolean isRaceResultAdded(RaceId raceId) {
		final String existCheck = "SELECT COUNT(*) FROM RaceResult WHERE race_number = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
	}
	
	/**
	 * Checks if race already exists.
	 * 
	 * @param raceId
	 * @return true if exists
	 */
	public boolean isRaceAdded(int raceId) {
		final String existCheck = "SELECT COUNT(*) FROM Race WHERE id = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
	}

	/**
	 * Adds name of driver to the Driver table in database.
	 * 
	 * @param driver name
	 */
	public void addDriver(String driver) {
		final String insertDriver = "INSERT OR IGNORE INTO Driver (name) VALUES (?)";
		jdbcTemplate.update(insertDriver, driver);
	}

	/**
	 * Appends the driver to DriverYear table in the given year.
	 * 
	 * @param driver name
	 * @param year of season
	 */
	public void addDriverYear(String driver, Year year) {
		addDriver(driver);
		int position = getMaxPosDriverYear(year) + 1;
		addDriverYear(new Driver(driver), year, position);
	}

	/**
	 * Gets the max position of drivers in the DriverYear table.
	 * 
	 * @param year of season
	 * @return max position. 0 if empty.
	 */
	public int getMaxPosDriverYear(Year year) {
		final String getMaxPos = "SELECT COALESCE(MAX(position), 0) FROM DriverYear WHERE year = ?";
		return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year);
	}

	/**
	 * Adds driver to the DriverYear table in the given year and position.
	 * 
	 * @param driver name
	 * @param year of season
	 * @param position of driver
	 */
	public void addDriverYear(Driver driver, Year year, int position) {
		final String addDriverYear = "INSERT INTO DriverYear (driver, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(addDriverYear, driver, year, position);
	}

	/**
	 * Removes driver from DriverYear table.
	 * 
	 * @param driver to delete
	 * @param year of season
	 */
	public void deleteDriverYear(Driver driver, Year year) {
		final String deleteDriver = "DELETE FROM DriverYear WHERE year = ? AND driver = ?";
		jdbcTemplate.update(deleteDriver, year, driver);
	}

	/**
	 * Removes all drivers from DriverYear table in the given year.
	 * 
	 * @param year of season
	 */
	public void deleteAllDriverYear(Year year) {
		final String deleteAllDrivers = "DELETE FROM DriverYear WHERE year = ?";
		jdbcTemplate.update(deleteAllDrivers, year);
	}
	
	/**
	 * Adds name of constructor to the Constructor table in database.
	 * 
	 * @param constructor name
	 */
	public void addConstructor(String constructor) {
		final String insertConstructor = "INSERT OR IGNORE INTO Constructor (name) VALUES (?)";
		jdbcTemplate.update(insertConstructor, constructor);
	}

	/**
	 * Appends the constructor to ConstructorYear table in the given year.
	 * 
	 * @param constructor name
	 * @param year of season
	 */
	public void addConstructorYear(String constructor, Year year) {
		addConstructor(constructor);
		int position = getMaxPosConstructorYear(year) + 1;
		addConstructorYear(new Constructor(constructor), year, position);
	}

	/**
	 * Gets the max position of constructors in the ConstructorYear table.
	 * 
	 * @param year of season
	 * @return max position. 0 if empty.
	 */
	public int getMaxPosConstructorYear(Year year) {
		final String getMaxPos = "SELECT COALESCE(MAX(position), 0) FROM ConstructorYear WHERE year = ?";
		return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year);
	}

	/**
	 * Adds constructor to the ConstructorYear table in the given year and position.
	 * 
	 * @param constructor name
	 * @param year of season
	 * @param position of constructor
	 */
	public void addConstructorYear(Constructor constructor, Year year, int position) {
		final String addConstructorYear = "INSERT INTO ConstructorYear (constructor, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(addConstructorYear, constructor, year, position);
	}

	/**
	 * Removes constructor from ConstructorYear table.
	 * 
	 * @param constructor to delete
	 * @param year of season
	 */
	public void deleteConstructorYear(Constructor constructor, Year year) {
		final String deleteConstructor = "DELETE FROM ConstructorYear WHERE year = ? AND constructor = ?";
		jdbcTemplate.update(deleteConstructor, year, constructor);
	}

	/**
	 * Removes all constructor from ConstructorYear table in the given year.
	 * 
	 * @param year of season
	 */
	public void deleteAllConstructorYear(Year year) {
		final String deleteAllConstructors = "DELETE FROM ConstructorYear WHERE year = ?";
		jdbcTemplate.update(deleteAllConstructors, year);
	}

	/**
	 * Insert driving to starting grid in given race and position.
	 * 
	 * @param raceId
	 * @param position of driver
	 * @param name of driver
	 */
	public void insertDriverStartingGrid(RaceId raceId, int position, Driver driver) {
		final String insertStartingGrid = "INSERT OR REPLACE INTO StartingGrid (race_number, position, driver) VALUES (?, ?, ?)";
		jdbcTemplate.update(insertStartingGrid, raceId, position, driver);
	}

	/**
	 * Inserts or replaces race result of driver into RaceResult table.
	 * 
	 * @param raceId of race
	 * @param position of driver
	 * @param driver name
	 * @param points
	 * @param finishingPosition the position that driver finished race in
	 */
	public void insertDriverRaceResult(RaceId raceId, String position, Driver driver, Points points, int finishingPosition) {
		final String insertRaceResult = "INSERT OR REPLACE INTO RaceResult (race_number, position, driver, points, finishing_position) VALUES (?, ?, ?, ?, ?)";
		jdbcTemplate.update(insertRaceResult, raceId, position, driver, points, finishingPosition);
	}

	/**
	 * Inserts or replaces position in standings of driver into DriverStandings table.
	 * 
	 * @param raceId of race
	 * @param driver name
	 * @param position of driver
	 * @param points of driver
	 */
	public void insertDriverIntoStandings(RaceId raceId, Driver driver, int position, Points points) {
		final String insertDriverStandings = "INSERT OR REPLACE INTO DriverStandings (race_number, driver, position, points) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(insertDriverStandings, raceId, driver, position, points);
	}
	
	/**
	 * Inserts or replaces position in standings of constructor into ConstructorStandings table.
	 * 
	 * @param raceId of race
	 * @param constructor name
	 * @param position of constructor
	 * @param points of constructor
	 */
	public void insertConstructorIntoStandings(RaceId raceId, Constructor constructor, int position, Points points) {
		final String insertConstructorStandings = "INSERT OR REPLACE INTO ConstructorStandings (race_number, constructor, position, points) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(insertConstructorStandings, raceId, constructor, position, points);
	}

	/**
	 * Inserts race id and name into Race table.
	 * 
	 * @param raceId of race
	 * @param raceName of race
	 */
	public void insertRace(int raceId, String raceName) {
		final String insertRaceName = "INSERT OR IGNORE INTO Race (id, name) VALUES (?, ?)";
		jdbcTemplate.update(insertRaceName, raceId, raceName);
	}

	/**
	 * Gets the max position of a race in RaceOrder of a given year.
	 * Is equivalent to number of races in the season.
	 * 
	 * @param year of season
	 * @return max position
	 */
	public int getMaxRaceOrderPosition(Year year) {
		final String sql = "SELECT MAX(position) FROM RaceOrder WHERE year = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, year);
	}

	/**
	 * Inserts race into RaceOrder.
	 * 
	 * @param raceId of race
	 * @param year of season
	 * @param position of race
	 */
	public void insertRaceOrder(RaceId raceId, int year, int position) {
		final String insertRaceOrder = "INSERT OR IGNORE INTO RaceOrder (id, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(insertRaceOrder, raceId, year, position);
	}

	/**
	 * Deletes race from Race table.
	 * 
	 * @param raceId to delete
	 */
	public void deleteRace(RaceId raceId) {
		final String deleteRace = "DELETE FROM Race WHERE id = ?";
		jdbcTemplate.update(deleteRace, raceId);
	}

	/**
	 * Checks if a season is a valid season. To be valid, it needs to have atleast one
	 * race in the RaceOrder table.
	 * 
	 * @param year of season
	 * @return true if season is valid
	 */
	public boolean isValidSeason(int year) {
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		return jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
	}

	/**
	 * Checks if a race is a valid race within a season. To be valid, it needs to have a
	 * table row in RaceOrder where both year and id are equal to input values.
	 * 
	 * @param raceId of race
	 * @param year of season
	 * @return true if race is valid
	 */
	public boolean isRaceInSeason(RaceId raceId, Year year) {
		final String validateRaceId = "SELECT COUNT(*) FROM RaceOrder WHERE year = ? AND id = ?";
		return jdbcTemplate.queryForObject(validateRaceId, Integer.class, year, raceId) > 0;
	}

	/**
	 * Gets a list of all valid years. I.E. years that are in RaceOrder table.
	 * Ordered descendingly.
	 * 
	 * @return valid years
	 */
	public List<Year> getAllValidYears() {
		final String sql = "SELECT DISTINCT year FROM RaceOrder ORDER BY year DESC";
		return jdbcTemplate.queryForList(sql, Integer.class).stream()
			.map(year -> new Year(year))
			.toList();
	}

	/**
	 * Gets a list of race ids from a season.
	 * Ordered by their position in RaceOrder.
	 * 
	 * @param year of season
	 * @return id of races
	 */
	public List<RaceId> getRacesFromSeason(Year year) {
		final String getRaceIds = "SELECT id FROM RaceOrder WHERE year = ? ORDER BY position ASC";
		return jdbcTemplate.queryForList(getRaceIds, Integer.class, year).stream()
			.map(id -> new RaceId(id))
			.toList();
	}

	/**
	 * Removes all races from RaceOrder in the given season.
	 * 
	 * @param year of season
	 */
	public void removeRaceOrderFromSeason(Year year) {
		final String removeOldOrderSql = "DELETE FROM RaceOrder WHERE year = ?";
		jdbcTemplate.update(removeOldOrderSql, year);
	}

	/**
	 * Gets the races from a season with their cutoff.
	 * Ordered ascendingly by race position.
	 * 
	 * @param year of season
	 * @return races with cutoff
	 */
	public List<CutoffRace> getCutoffRaces(Year year) {
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
			RaceId raceId = new RaceId(id);
			CutoffRace race = new CutoffRace(position, name, raceId, cutoff, year);
			races.add(race);
		}
		return races;
	}

	/**
	 * Gets the cutoff of the year in LocalDataTime.
	 * 
	 * @param year of season
	 * @return cutoff time in local time
	 */
	public LocalDateTime getCutoffYearLocalTime(Year year) {
		return TimeUtil.instantToLocalTime(getCutoffYear(year));
	}

	/**
	 * Sets the cutoff of the given race to the given time.
	 * 
	 * @param cutoffTime for guessing
	 * @param raceId of race
	 */
	public void setCutoffRace(Instant cutoffTime, RaceId raceId) {
		final String setCutoffTime = "INSERT OR REPLACE INTO RaceCutoff (race_number, cutoff) VALUES (?, ?)";
		jdbcTemplate.update(setCutoffTime, raceId, cutoffTime.toString());
	}

	/**
	 * Sets the cutoff of the given season to the given time.
	 * 
	 * @param cutoffTime for guessing
	 * @param year of season
	 */
	public void setCutoffYear(Instant cutoffTime, Year year) {
		final String setCutoffTime = "INSERT OR REPLACE INTO YearCutoff (year, cutoff) VALUES (?, ?)";
		jdbcTemplate.update(setCutoffTime, year, cutoffTime);
	}

	/**
	 * Gets a list of registered flags for a given race.
	 * 
	 * @param raceId of race
	 * @return registered flags
	 */
	public List<RegisteredFlag> getRegisteredFlags(RaceId raceId) {
		List<RegisteredFlag> registeredFlags = new ArrayList<>();
		final String getRegisteredFlags = "SELECT flag, round, id FROM FlagStats WHERE race_number = ? ORDER BY round";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRegisteredFlags, raceId);
		for (Map<String, Object> row : sqlRes) {
			Flag type = new Flag((String) row.get("flag"));
			int round = (int) row.get("round");
			int id = (int) row.get("id");

			registeredFlags.add(new RegisteredFlag(type, round, id));
		}
		return registeredFlags;
	}

	/**
	 * Inserts an instance of a recorded flag to the database.
	 * IDs are assigned automatically.
	 * 
	 * @param flag type of flag
	 * @param round the round flag happened in
	 * @param raceId of race
	 */
	public void insertFlagStats(Flag flag, int round, RaceId raceId) {
		final String sql = "INSERT INTO FlagStats (flag, race_number, round) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, flag, raceId, round);
	}

	/**
	 * Deletes a recorded flag by its id.
	 * 
	 * @param flagId of stat
	 */
	public void deleteFlagStatsById(int flagId) {
		final String sql = "DELETE FROM FlagStats WHERE id = ?";
		jdbcTemplate.update(sql, flagId);
	}

	/**
	 * Gets the name of a race.
	 * 
	 * @param raceId of race
	 * @return name of race
	 */
	public String getRaceName(RaceId raceId) {
		final String getRaceNameSql = "SELECT name FROM Race WHERE id = ?";
		return jdbcTemplate.queryForObject(getRaceNameSql, String.class, raceId);
	}

	/**
	 * Gets a list of all the types of flags.
	 * 
	 * @return name of flag types
	 */
	public List<Flag> getFlags() {
		final String sql = "SELECT name FROM Flag";
		return jdbcTemplate.queryForList(sql, String.class).stream()
			.map(flag -> new Flag(flag))
			.toList();
	}

	/**
	 * Gets a list of the starting grid of a race as a list of PositionedCompetitor.
	 * Points are left blank.
	 * 
	 * @param raceId of race
	 * @return starting grid
	 */
	public List<PositionedCompetitor> getStartingGrid(RaceId raceId) {
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

	/**
	 * Gets a list of the race result of a race as a list of PositionedCompetitor.
	 * 
	 * @param raceId of race
	 * @return race result
	 */
	public List<PositionedCompetitor> getRaceResult(RaceId raceId) {
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

	/**
	 * Gets a list of the driver standings from a race as a list of PositionedCompetitor.
	 * 
	 * @param raceId of race
	 * @return driver standings
	 */
	public List<PositionedCompetitor> getDriverStandings(RaceId raceId) {
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
	
	/**
	 * Gets a list of the constructor standings from a race as a list of PositionedCompetitor.
	 * 
	 * @param raceId of race
	 * @return constructor standings
	 */
	public List<PositionedCompetitor> getConstructorStandings(RaceId raceId) {
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

	/**
	 * Checks if a driver is in DriverYear in the given season.
	 * 
	 * @param driver the name of the driver
	 * @param year of season
	 * @return true if driver is valid
	 */
	public boolean isValidDriverYear(Driver driver, Year year) {
		final String existCheck = "SELECT COUNT(*) FROM DriverYear WHERE year = ? AND driver = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, year, driver) > 0;
	}

	public boolean isValidDriver(Driver driver) {
		final String existCheck = "SELECT COUNT(*) FROM Driver WHERE name = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, driver) > 0;
	}
	
	/**
	 * Checks if a constructor is in ConstructorYear in the given season.
	 * 
	 * @param constructor the name of the constructor
	 * @param year of season
	 * @return true if constructor is valid
	 */
	public boolean isValidConstructorYear(Constructor constructor, Year year) {
		final String existCheck = "SELECT COUNT(*) FROM ConstructorYear WHERE year = ? AND constructor = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, year, constructor) > 0;
	}
	
	public boolean isValidConstructor(Constructor constructor) {
		final String existCheck = "SELECT COUNT(*) FROM Constructor WHERE name = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, constructor) > 0;
	}

	public boolean isValidFlag(String value) {
		final String existCheck = "SELECT COUNT(*) FROM Flag WHERE name = ?";
		return jdbcTemplate.queryForObject(existCheck, Integer.class, value) > 0;
	}

	private void addToMailingList(UUID userId, String email) {
		final String sql = "INSERT OR REPLACE INTO MailingList (user_id, email) VALUES (?, ?)";
		jdbcTemplate.update(sql, userId, email);
		removeVerificationCode(userId);
	}
	
	public void clearUserFromMailing(UUID userId) {
		clearMailPreferences(userId);
		clearNotified(userId);
		deleteUserFromMailingList(userId);
	}
	
	private void deleteUserFromMailingList(UUID userId) {
		final String sql = "DELETE FROM MailingList WHERE user_id = ?";
		jdbcTemplate.update(sql, userId);
	}


	public boolean userHasEmail(UUID userId) {
		final String sql = "SELECT COUNT(*) FROM MailingList WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
	}

	public String getEmail(UUID userId) {
		final String sql = "SELECT email FROM MailingList WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, String.class, userId);	
	}

	public List<UserMail> getMailingList(RaceId raceId) {
		final String sql = """
			SELECT u.google_id as google_id, u.id as id, u.username as username, ml.email as email
			FROM User u
			JOIN MailingList ml ON ml.user_id = u.id
			WHERE u.id NOT IN (SELECT guesser FROM DriverPlaceGuess WHERE race_number = ? GROUP BY guesser HAVING COUNT(*) == 2);
			""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, raceId);
		return sqlRes.stream()
			.map(row -> 
			new UserMail(
				new User(
					(String) row.get("google_id"),
					UUID.fromString((String) row.get("id")),
					(String) row.get("username"))
				, (String) row.get("email"))
			)
			.toList();
	}

	public void setNotified(RaceId raceId, UUID userId) {
		final String sql = "INSERT OR IGNORE INTO Notified (user_id, race_number) VALUES (?, ?)";
		jdbcTemplate.update(sql, userId, raceId);
	}

	public int getNotifiedCount(RaceId raceId, UUID userId) {
		final String sql = "SELECT COUNT(*) FROM Notified WHERE user_id = ? AND race_number = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, userId, raceId);
	}

	private void clearNotified(UUID userId) {
		final String sql = "DELETE FROM Notified WHERE user_id = ?";
		jdbcTemplate.update(sql, userId);
	}

	public void addVerificationCode(UserMail userMail, int verificationCode) {
		final String sql = """
			INSERT OR REPLACE INTO VerificationCode (user_id, verification_code, email, cutoff) VALUES (?, ?, ?, ?)""";
		jdbcTemplate.update(sql, userMail.user.id, verificationCode, userMail.email, Instant.now().plus(Duration.ofMinutes(10)).toString());
	}

	public void removeVerificationCode(UUID userId) {
		final String sql = "DELETE FROM VerificationCode WHERE user_id = ?";
		jdbcTemplate.update(sql, userId);
	}

	public void removeExpiredVerificationCodes() {
		final String sql = "SELECT cutoff, user_id FROM VerificationCode";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql);
		
		List<UUID> expired = sqlRes.stream()
			.filter(row -> Instant.parse((String) row.get("cutoff")).compareTo(Instant.now()) < 0)
			.map(row -> UUID.fromString((String) row.get("user_id")))
			.toList();
		for (UUID userId : expired) {
			removeVerificationCode(userId);
		}
	}

	public boolean hasVerificationCode(UUID userId) {
		final String sql = "SELECT COUNT(*) FROM VerificationCode WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
	}

	public boolean isValidVerificationCode(UUID userId, int verificationCode) {
		final String sql = "SELECT COUNT(*) FROM VerificationCode WHERE user_id = ? AND verification_code = ?";
		boolean isValidCode = jdbcTemplate.queryForObject(sql, Integer.class, userId, verificationCode) > 0;
		if (!isValidCode) {
			return false;
		}
		final String getCutoffSql = "SELECT cutoff FROM VerificationCode WHERE user_id = ?";
		Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoffSql, String.class, userId));
		boolean isValidCutoff = cutoff.compareTo(Instant.now()) > 0;
		if (!isValidCutoff) {
			return false;
		}
		final String emailSql = "SELECT email FROM VerificationCode WHERE user_id = ?";
		String email = jdbcTemplate.queryForObject(emailSql, String.class, userId);
		addToMailingList(userId, email);
		return true;
	}

	public List<List<String>> userGuessDataDriver(UUID userId) {
		final String sql = """
			SELECT position, driver, year
			FROM DriverGuess
			WHERE guesser = ?
			ORDER BY year DESC, position ASC
			""";
		return jdbcTemplate.queryForList(sql, userId).stream()
			.map(row -> Arrays.asList(
				String.valueOf((int) row.get("position")),
				(String) row.get("driver"),
				String.valueOf((int) row.get("year"))
			)).toList();
	}

	public List<List<String>> userGuessDataConstructor(UUID userId) {
		final String sql = """
			SELECT position, constructor, year
			FROM ConstructorGuess
			WHERE guesser = ?
			ORDER BY year DESC, position ASC
			""";
		return jdbcTemplate.queryForList(sql, userId).stream()
			.map(row -> Arrays.asList(
				String.valueOf((int) row.get("position")),
				(String) row.get("constructor"),
				String.valueOf((int) row.get("year"))
			)).toList();
	}

	public List<List<String>> userGuessDataFlag(UUID userId) {
		final String sql = """
			SELECT flag, amount, year
			FROM FlagGuess
			WHERE guesser = ?
			ORDER BY year DESC, flag ASC
			""";
		return jdbcTemplate.queryForList(sql, userId).stream()
			.map(row -> Arrays.asList(
				translateFlagName(new Flag((String) row.get("flag"))),
				String.valueOf((int) row.get("amount")),
				String.valueOf((int) row.get("year"))
			)).toList();

	}

	public List<List<String>> userGuessDataDriverPlace(UUID userId) {
		final String sql = """
			SELECT dpg.category AS category, dpg.driver AS driver, r.name AS race_name, ro.year AS year
			FROM DriverPlaceGuess dpg
			JOIN Race r ON dpg.race_number = r.id
			JOIN RaceOrder ro ON dpg.race_number = ro.id
			WHERE dpg.guesser = ?
			ORDER BY ro.year DESC, ro.position ASC, category ASC
			""";
		return jdbcTemplate.queryForList(sql, userId).stream()
		.map(row -> Arrays.asList(
			translateCategory(new Category((String) row.get("category"))),
			(String) row.get("driver"),
			(String) row.get("race_name"),
			String.valueOf((int) row.get("year"))
		)).toList();
	}

	public List<List<String>> userDataNotified(UUID userId) {
		final String sql = """
			SELECT r.name AS name, count(*) as notified_count, ro.year AS year
			FROM Notified n
			JOIN Race r ON n.race_number = r.id
			JOIN RaceOrder ro ON n.race_number = ro.id
			WHERE n.user_id = ?
			GROUP BY n.race_number
			ORDER BY ro.year DESC, ro.position ASC
			""";
		return jdbcTemplate.queryForList(sql, userId).stream()
		.map(row -> Arrays.asList(
			(String) row.get("name"),
			String.valueOf((int) row.get("notified_count")),
			String.valueOf((int) row.get("year"))
		)).toList();
	}

	public boolean isValidMailOption(int option) {
		final String sql = "SELECT COUNT(*) FROM MailOption WHERE option = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, option) > 0;
	}

	public void addMailOption(UUID userId, MailOption option) {
		final String sql = "INSERT OR IGNORE INTO MailPreference (user_id, option) VALUES (?, ?)";
		jdbcTemplate.update(sql, userId, option);
	}

	public void removeMailOption(UUID userId, MailOption option) {
		final String sql = "DELETE FROM MailPreference WHERE user_id = ? AND option = ?";
		jdbcTemplate.update(sql, userId, option);
	}

	private void clearMailPreferences(UUID userId) {
		final String sql = "DELETE FROM MailPreference WHERE user_id = ?";
		jdbcTemplate.update(sql, userId);
	}

	public List<MailOption> getMailingPreference(UUID userId) {
		final String sql = "SELECT option FROM MailPreference WHERE user_id = ? ORDER BY option DESC";
		return jdbcTemplate.queryForList(sql, Integer.class, userId).stream()
			.map(option -> new MailOption(option))
			.toList();
	}
	
	public List<MailOption> getMailingOptions() {
		final String sql = "SELECT option FROM MailOption ORDER BY option ASC";
		return jdbcTemplate.queryForList(sql, Integer.class).stream()
			.map(option -> new MailOption(option))
			.toList();
	}

	public void setTeamDriver(Driver driver, Constructor team, Year year) {
		final String sql = "INSERT OR REPLACE INTO DriverTeam (driver, team, year) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, driver, team, year);
	}
	
	public void addColorConstructor(Constructor constructor, Year year, Color color) {
		if (color.value == null) {
			return;
		}
		final String sql = "INSERT OR REPLACE INTO ConstructorColor (constructor, year, color) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, constructor, year, color);
	}
}
