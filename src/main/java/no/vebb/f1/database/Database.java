package no.vebb.f1.database;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.util.collection.userTables.Summary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.*;
import no.vebb.f1.util.collection.*;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserMail;

@Service
@SuppressWarnings("DataFlowIssue")
public class Database {

    private final JdbcTemplate jdbcTemplate;

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
        final String getCutoff = "SELECT cutoff FROM year_cutoffs WHERE year = ?;";
        return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, year.value));
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
            final String getCutoff = "SELECT cutoff FROM race_cutoffs WHERE race_id = ?;";
            return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceId.value));
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
        final String sql = "SELECT COUNT(*) FROM admins WHERE user_id = ?;";
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
        final String sql = "SELECT username, google_id FROM users WHERE user_id = ?;";
        Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, userId);
        String username = sqlRes.get("username").toString();
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
        final String sql = "SELECT username, user_id FROM users WHERE google_id = ?;";
        Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, googleId);
        String username = sqlRes.get("username").toString();
        UUID id = (UUID) sqlRes.get("user_id");
        return new User(googleId, id, username);
    }

    /**
     * Gets the latest race number that has had a race.
     *
     * @param year of season
     * @return race number of race
     */
    public RaceId getLatestRaceId(Year year) throws EmptyResultDataAccessException {
        final String getRaceIdSql = """
                SELECT ro.race_id
                FROM race_order ro
                JOIN race_results rr ON ro.race_id = rr.race_id
                WHERE ro.year = ?
                ORDER BY ro.position DESC
                LIMIT 1;
                """;

        return new RaceId(jdbcTemplate.queryForObject(getRaceIdSql, Integer.class, year.value));
    }

    /**
     * Gets the position of a race within the season it is in.
     *
     * @param raceId of race
     * @return position of race
     */
    public int getPositionOfRace(RaceId raceId) {
        final String getRacePosition = "SELECT position FROM race_order WHERE race_id = ?;";
        return jdbcTemplate.queryForObject(getRacePosition, Integer.class, raceId.value);
    }

    /**
     * Gets the data for a users guesses on flags up until the given race position
     * of the given year. If race position is 0, actual amount will also be 0.
     *
     * @param racePos position within a season
     * @param year    of season
     * @param userId  of guesser/user
     * @return "table" of guesses
     */
    public List<Map<String, Object>> getDataForFlagTable(int racePos, Year year, UUID userId) {
        if (racePos == 0) {
            final String sqlNoRace = """
                    SELECT f.flag_name AS type, fg.amount AS guessed, 0::INTEGER AS actual
                    FROM flags f
                    JOIN flag_guesses fg ON f.flag_name = fg.flag_name
                    JOIN race_order ro ON fg.year = ro.year
                    WHERE ro.year = ? AND fg.user_id = ?
                    GROUP BY f.flag_name, fg.amount;
                    """;
            return jdbcTemplate.queryForList(sqlNoRace, year.value, userId);
        } else {
            final String sql = """
                    SELECT f.flag_name AS type, fg.amount AS guessed, COALESCE(COUNT(fs.flag_name), 0)::INTEGER AS actual
                    FROM flags f
                    JOIN flag_guesses fg ON f.flag_name = fg.flag_name
                    JOIN race_order ro ON fg.year = ro.year
                    LEFT JOIN flag_stats fs ON fs.flag_name = f.flag_name AND fs.race_id = ro.race_id
                    WHERE ro.year = ? AND fg.user_id = ? AND ro.position <= ?
                    GROUP BY f.flag_name, fg.amount;
                    """;
            return jdbcTemplate.queryForList(sql, year.value, userId, racePos);
        }
    }

    /**
     * Gets the data for a users guesses on the given race of the given year.
     * Columns: race_position, race_name, driver, start, finish
     *
     * @param category to get table for
     * @param userId   of guesser/user
     * @param year     of season
     * @param racePos  position within a season
     * @return "table" of guesses
     */
    public List<Map<String, Object>> getDataForPlaceGuessTable(Category category, UUID userId, Year year, int racePos) {
        final String sql = """
                SELECT ro.position as race_position, r.race_name AS race_name, dpg.driver_name AS driver, sg.position AS start, rr.finishing_position AS finish
                FROM driver_place_guesses dpg
                JOIN races r ON r.race_id = dpg.race_id
                JOIN race_order ro ON r.race_id = ro.race_id
                JOIN starting_grids sg ON sg.race_id = r.race_id AND dpg.driver_name = sg.driver_name
                JOIN race_results rr ON rr.race_id = r.race_id AND dpg.driver_name = rr.driver_name
                WHERE dpg.category_name = ? AND dpg.user_id = ? AND ro.year = ? AND ro.position <= ?
                ORDER BY ro.position;
                """;
        return jdbcTemplate.queryForList(sql, category.value, userId, year.value, racePos);
    }

    /**
     * Gets a list of drivers guessed by the given user in the given year.
     * Ordered by position of guesses in ascending order.
     *
     * @param year   of season
     * @param userId of user
     * @return drivers ascendingly
     */
    public List<Driver> getGuessedYearDriver(Year year, UUID userId) {
        final String guessedSql = "SELECT driver_name FROM driver_guesses WHERE year = ?  AND user_id = ? ORDER BY position";
        return jdbcTemplate.queryForList(guessedSql, String.class, year.value, userId).stream()
                .map(Driver::new)
                .toList();
    }

    /**
     * Gets a list of constructors guessed by the given user in the given year.
     * Ordered by position of guesses in ascending order.
     *
     * @param year   of season
     * @param userId of user
     * @return constructors ascendingly
     */
    public List<Constructor> getGuessedYearConstructor(Year year, UUID userId) {
        final String guessedSql = """
                SELECT constructor_name
                FROM constructor_guesses
                WHERE year = ? AND user_id = ?
                ORDER BY position;
                """;
        return jdbcTemplate.queryForList(guessedSql, String.class, year.value, userId).stream()
                .map(Constructor::new)
                .toList();
    }

    /**
     * Gets the driver standings for a given race and year.
     * If the race id is set to -1, the position set in the DriverYear will be used as
     * default order.
     * Ordered by position of standings in ascending order.
     *
     * @param raceId of race
     * @param year   of season
     * @return drivers ascendingly
     */
    public List<Driver> getDriverStandings(RaceId raceId, Year year) {
        final String driverYearSql = "SELECT driver_name FROM drivers_year WHERE year = ? ORDER BY position;";
        final String driverStandingsSql = "SELECT driver_name FROM driver_standings WHERE race_id = ? ORDER BY position;";
        List<String> result = getCompetitors(raceId, year, driverYearSql, driverStandingsSql);
        return result.stream().map(Driver::new).toList();
    }

    private List<String> getCompetitors(RaceId raceId, Year year, final String competitorYearSql, final String competitorStandingsSql) {
        List<String> result;
        if (raceId == null) {
            result = jdbcTemplate.queryForList(competitorYearSql, String.class, year.value);
        } else {
            result = jdbcTemplate.queryForList(competitorStandingsSql, String.class, raceId.value);
        }
        return result;
    }

    /**
     * Gets the constructor standings for a given race and year.
     * If the race number is set to -1, the position set in the ConstructorYear will be used as
     * default order.
     * Ordered by position of standings in ascending order.
     *
     * @param raceId of race
     * @param year   of season
     * @return constructors ascendingly
     */
    public List<Constructor> getConstructorStandings(RaceId raceId, Year year) {
        final String constructorYearSql = "SELECT constructor_name FROM constructors_year WHERE year = ? ORDER BY position;";
        final String constructorStandingsSql = """
                SELECT constructor_name
                FROM constructor_standings
                WHERE race_id = ?
                ORDER BY position;
                """;
        List<String> result = getCompetitors(raceId, year, constructorYearSql, constructorStandingsSql);
        return result.stream().map(Constructor::new).toList();
    }

    /**
     * Checks if the username is already in use by a user.
     *
     * @param username the username
     * @return true if username is in use
     */
    public boolean isUsernameInUse(String username) {
        final String sqlCheckUsername = "SELECT COUNT(*) FROM users WHERE username = ?::citext;";
        return jdbcTemplate.queryForObject(sqlCheckUsername, Integer.class, username) > 0;
    }

    /**
     * Updates the username of the given user to the given username.
     *
     * @param username to set as new username
     * @param userId   of user
     */
    public void updateUsername(Username username, UUID userId) {
        final String updateUsername = """
                UPDATE users
                SET username = ?
                WHERE user_id = ?;
                """;
        jdbcTemplate.update(updateUsername, username.username, userId);
    }

    /**
     * Deletes the account of the given user.
     * Sets the username to 'Anonym' and google_id to id.
     *
     * @param userId of user
     */
    public void deleteUser(UUID userId) {
        final String deleteUser = """
                UPDATE users
                SET username = 'Anonym' || nextVal('anonymous_username_seq'), google_id = ?
                WHERE user_id = ?;
                """;
        clearUserFromMailing(userId);
        removeBingomaster(userId);
        jdbcTemplate.update(deleteUser, userId, userId);
    }

    /**
     * Adds a user with the given username and google ID to the database.
     * Sets a random UUID as the users ID.
     *
     * @param username of the user
     * @param googleId the ID provided by OAUTH
     */
    public void addUser(Username username, String googleId) {
        final String sqlInsertUsername = "INSERT INTO users (google_id, user_id, username) VALUES (?, ?, ?);";
        jdbcTemplate.update(sqlInsertUsername, googleId, UUID.randomUUID(), username.username);
    }

    /**
     * Gets the guesses of all users for the given race in the given category.
     *
     * @param raceId   of race
     * @param category for guesses
     * @return list of guesses
     */
    public List<UserRaceGuess> getUserGuessesDriverPlace(RaceId raceId, Category category) {
        final String getGuessSql = """
                SELECT u.username AS username, dpg.driver_name AS driver, sg.position AS position
                FROM driver_place_guesses dpg
                JOIN users u ON u.user_id = dpg.user_id
                JOIN starting_grids sg ON sg.race_id = dpg.race_id AND sg.driver_name = dpg.driver_name
                WHERE dpg.race_id = ? AND dpg.category_name = ?
                ORDER BY u.username;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getGuessSql, raceId.value, category.value);
        return sqlRes.stream()
                .map(row -> new UserRaceGuess(
                        row.get("username").toString(),
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
                SELECT ro.race_id AS id, ro.position AS position, r.race_name AS name
                FROM race_order ro
                JOIN starting_grids sg ON ro.race_id = sg.race_id
                JOIN races r ON r.race_id = ro.race_id
                WHERE ro.year = ?
                ORDER BY ro.position DESC
                LIMIT 1;
                """;
        Map<String, Object> res = jdbcTemplate.queryForMap(getRaceIdSql, year.value);
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
                SELECT DISTINCT race_id
                FROM starting_grids sg
                WHERE sg.race_id NOT IN (
                	SELECT rr.race_id
                	FROM race_results rr
                );
                """;
        return new RaceId(jdbcTemplate.queryForObject(getRaceId, Integer.class));
    }

    /**
     * Adds the guesses of flags of a user into the given year.
     * Overwrites pre-existing guesses.
     *
     * @param userId of user
     * @param year   of season
     * @param flags  the user guessed
     */
    public void addFlagGuesses(UUID userId, Year year, Flags flags) {
        final String sql = """
            INSERT INTO flag_guesses (user_id, flag_name, year, amount)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, flag_name, year)
            DO UPDATE SET amount = EXCLUDED.amount;
        """;
        jdbcTemplate.update(sql, userId, "Yellow Flag", year.value, flags.yellow);
        jdbcTemplate.update(sql, userId, "Red Flag", year.value, flags.red);
        jdbcTemplate.update(sql, userId, "Safety Car", year.value, flags.safetyCar);
    }

    /**
     * Gets the flag guesses of the given user in the given year.
     *
     * @param userId of user
     * @param year   of season
     * @return flag guesses
     */
    public Flags getFlagGuesses(UUID userId, Year year) {
        final String sql = "SELECT flag_name, amount FROM flag_guesses WHERE user_id = ? AND year = ?;";
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, userId, year.value);
        Flags flags = new Flags();
        for (Map<String, Object> row : sqlRes) {
            String flag = (String) row.get("flag_name");
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
        final String getCutoff = "SELECT cutoff FROM race_cutoffs WHERE race_id = ?;";
        Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceId.value));
        return Duration.between(now, cutoff).toSeconds();
    }

    public int getTimeLeftToGuessRaceHours(RaceId raceId) {
        return (int) (getTimeLeftToGuessRace(raceId) / 3600L);
    }

    /**
     * Gets number of seconds remaining to guess in the year.
     *
     * @return time left in seconds
     */
    public long getTimeLeftToGuessYear() {
        Instant now = Instant.now();
        final String getCutoff = "SELECT cutoff FROM year_cutoffs WHERE year = ?;";
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
        final String getDriversFromGrid = "SELECT driver_name FROM starting_grids WHERE race_id = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getDriversFromGrid, String.class, raceId.value).stream()
                .map(Driver::new)
                .toList();
    }

    public List<ColoredCompetitor<Driver>> getDriversFromStartingGridWithColors(RaceId raceId) {
        final String getDriversFromGrid = """
                SELECT sg.driver_name as driver, cc.color as color
                FROM starting_grids sg
                LEFT JOIN drivers_team dt ON dt.driver_name = sg.driver_name
                LEFT JOIN constructors_color cc ON cc.constructor_name = dt.team
                WHERE race_id = ?
                ORDER BY position;
                """;
        return jdbcTemplate.queryForList(getDriversFromGrid, raceId.value).stream()
                .map(row ->
                        new ColoredCompetitor<>(
                                new Driver((String) row.get("driver")),
                                new Color((String) row.get("color")))
                )
                .toList();
    }

    /**
     * Gets the previous guess of a user on driver place guess.
     *
     * @param raceId   of race
     * @param category guessed on
     * @param userId   of the user
     * @return name of driver guessed
     */
    public Driver getGuessedDriverPlace(RaceId raceId, Category category, UUID userId) {
        final String getPreviousGuessSql = """
                SELECT driver_name
                FROM driver_place_guesses
                WHERE race_id = ? AND category_name = ? AND user_id = ?;
                """;
        return new Driver(jdbcTemplate.queryForObject(getPreviousGuessSql, String.class, raceId.value, category.value, userId));
    }

    /**
     * Adds driver place guess to the database.
     *
     * @param userId   of the guesser
     * @param raceId   of race
     * @param driver   name guessed
     * @param category which the user guessed on
     */
    public void addDriverPlaceGuess(UUID userId, RaceId raceId, Driver driver, Category category) {
        final String insertGuessSql = """
            INSERT INTO driver_place_guesses (user_id, race_id, driver_name, category_name)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, race_id, category_name)
            DO UPDATE SET driver_name = EXCLUDED.driver_name;
        """;
        jdbcTemplate.update(insertGuessSql, userId, raceId.value, driver.value, category.value);
    }

    /**
     * Gets a list of a users guesses on a drivers in a given season.
     *
     * @param userId of user
     * @param year   of season
     * @return competitors ascendingly
     */
    public List<ColoredCompetitor<Driver>> getDriversGuess(UUID userId, Year year) {
        final String getGuessedSql = """
                SELECT dg.driver_name as driver, cc.color as color
                FROM driver_guesses dg
                LEFT JOIN drivers_team dt ON dt.driver_name = dg.driver_name
                LEFT JOIN constructors_color cc ON cc.constructor_name = dt.team
                WHERE dg.user_id = ?
                ORDER BY position;
                """;
        final String getDriversSql = """
                SELECT dy.driver_name as driver, cc.color as color
                FROM drivers_year dy
                LEFT JOIN drivers_team dt ON dt.driver_name = dy.driver_name
                LEFT JOIN constructors_color cc ON cc.constructor_name = dt.team
                WHERE dy.year = ?
                ORDER BY position;
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
     * @param year   of season
     * @return competitors ascendingly
     */
    public List<ColoredCompetitor<Constructor>> getConstructorsGuess(UUID userId, Year year) {
        final String getGuessedSql = """
                SELECT cg.constructor_name as constructor, cc.color as color
                FROM constructor_guesses cg
                LEFT JOIN constructors_color cc ON cc.constructor_name = cg.constructor_name
                WHERE cg.user_id = ?
                ORDER BY position;
                """;
        final String getConstructorsSql = """
                SELECT cy.constructor_name as constructor, cc.color as color
                FROM constructors_year cy
                LEFT JOIN constructors_color cc ON cc.constructor_name = cy.constructor_name
                WHERE cy.year = ?
                ORDER BY position;
                """;
        return getCompetitorGuess(userId, year, getGuessedSql, getConstructorsSql).stream()
                .map(row -> new ColoredCompetitor<>(
                        new Constructor((String) row.get("constructor")),
                        new Color((String) row.get("color"))))
                .toList();
    }

    private List<Map<String, Object>> getCompetitorGuess(
            UUID userId,
            Year year,
            final String getGuessedSql,
            final String getCompetitorsSql) {
        List<Map<String, Object>> competitors = jdbcTemplate.queryForList(getGuessedSql, userId);
        if (competitors.isEmpty()) {
            return jdbcTemplate.queryForList(getCompetitorsSql, year.value);
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
        final String getDriversSql = "SELECT driver_name FROM drivers_year WHERE year = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getDriversSql, String.class, year.value).stream()
                .map(Driver::new)
                .toList();
    }

    /**
     * Gets a list of a yearly constructors in a given season.
     *
     * @param year of season
     * @return constructors ascendingly
     */
    public List<Constructor> getConstructorsYear(Year year) {
        final String getConstructorSql = "SELECT constructor_name FROM constructors_year WHERE year = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getConstructorSql, String.class, year.value).stream()
                .map(Constructor::new)
                .toList();
    }

    /**
     * Adds a guess for a user on the ranking of a driver.
     *
     * @param userId   of user
     * @param driver   name
     * @param year     of season
     * @param position guessed
     */
    public void insertDriversYearGuess(UUID userId, Driver driver, Year year, int position) {
        final String addRowDriver = """
            INSERT INTO driver_guesses (user_id, driver_name, year, position)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, position, year)
            DO UPDATE SET driver_name = EXCLUDED.driver_name;
        """;
        jdbcTemplate.update(addRowDriver, userId, driver.value, year.value, position);
    }

    /**
     * Adds a guess for a user on the ranking of a constructor.
     *
     * @param userId      of user
     * @param constructor name
     * @param year        of season
     * @param position    guessed
     */
    public void insertConstructorsYearGuess(UUID userId, Constructor constructor, Year year, int position) {
        final String addRowConstructor = """
            INSERT INTO constructor_guesses (user_id, constructor_name, year, position)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, position, year)
            DO UPDATE SET constructor_name = EXCLUDED.constructor_name;
         """;
        jdbcTemplate.update(addRowConstructor, userId, constructor.value, year.value, position);
    }

    /**
     * Gets a list of all guessing categories.
     *
     * @return categories
     */
    public List<Category> getCategories() {
        final String sql = "SELECT category_name FROM categories;";
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
        final String validateCategory = "SELECT COUNT(*) FROM categories WHERE category_name = ?;";
        return jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
    }

    /**
     * Gets a mapping from the difference of a guess to the points
     * obtained by the difference in a given category.
     *
     * @param category name
     * @param year     of season
     * @return map from diff to points
     */
    public Map<Diff, Points> getDiffPointsMap(Year year, Category category) {
        final String sql = """
                SELECT points, diff
                FROM diff_points_mappings
                WHERE year = ? AND category_name = ?
                ORDER BY diff;
                """;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, year.value, category.value);
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
     * @param year     of season
     * @param category name
     * @return max diff
     */
    public Diff getMaxDiffInPointsMap(Year year, Category category) {
        final String getMaxDiff = "SELECT MAX(diff) FROM diff_points_mappings WHERE year = ? AND category_name = ?;";
        return new Diff(jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year.value, category.value));
    }

    /**
     * Adds a new mapping from the given diff to 0 points in the given season and category.
     *
     * @param category name
     * @param diff     to add mapping for
     * @param year     of season
     */
    public void addDiffToPointsMap(Category category, Diff diff, Year year) {
        final String addDiff = "INSERT INTO diff_points_mappings (category_name, diff, points, year) VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(addDiff, category.value, diff.value, 0, year.value);
    }

    /**
     * Removes the mapping from the given diff in the given season and category.
     *
     * @param category name
     * @param diff     to remove mapping for
     * @param year     of season
     */
    public void removeDiffToPointsMap(Category category, Diff diff, Year year) {
        final String deleteRowWithDiff = "DELETE FROM diff_points_mappings WHERE year = ? AND category_name = ? AND diff = ?;";
        jdbcTemplate.update(deleteRowWithDiff, year.value, category.value, diff.value);
    }

    /**
     * Sets a new mapping from the given diff to the given points in the given season and category.
     *
     * @param category name
     * @param diff     to set new mapping for
     * @param year     of season
     * @param points   for getting the diff
     */
    public void setNewDiffToPointsInPointsMap(Category category, Diff diff, Year year, Points points) {
        final String setNewPoints = """
                UPDATE diff_points_mappings
                SET points = ?
                WHERE diff = ? AND year = ? AND category_name = ?;
                """;
        jdbcTemplate.update(setNewPoints, points.value, diff.value, year.value, category.value);
    }

    /**
     * Checks if there is a diff set for the given season and category.
     *
     * @param category name
     * @param diff     to check
     * @param year     of season
     */
    public boolean isValidDiffInPointsMap(Category category, Diff diff, Year year) {
        final String validateDiff = "SELECT COUNT(*) FROM diff_points_mappings WHERE year = ? AND category_name = ? AND diff = ?;";
        return jdbcTemplate.queryForObject(validateDiff, Integer.class, year.value, category.value, diff.value) > 0;
    }

    /**
     * Gets a list of all all users sorted by username_upper.
     *
     * @return every user
     */
    public List<User> getAllUsers() {
        final String getAllUsersSql = """
                SELECT user_id, username, google_id
                FROM users
                ORDER BY username;
                """;
        return jdbcTemplate.queryForList(getAllUsersSql).stream()
                .map(row ->
                        new User(
                                (String) row.get("google_id"),
                                (UUID) row.get("user_id"),
                                row.get("username").toString())
                ).toList();
    }

    /**
     * Gets a list of every person that has guessed in a season. To qualify they have to have guessed
     * on flags, drivers and constructors.
     * Ordered ascendingly by username
     *
     * @param year of season
     * @return id of guessers
     */
    public List<User> getSeasonGuessers(Year year) {
        final String getGussers = """
                SELECT DISTINCT u.user_id as id, u.username
                FROM users u
                JOIN flag_guesses fg ON fg.user_id = u.user_id
                JOIN driver_guesses dg ON dg.user_id = u.user_id
                JOIN constructor_guesses cg ON cg.user_id = u.user_id
                WHERE fg.year = ? AND dg.year = ? AND cg.year = ?
                ORDER BY u.username;
                """;

        return jdbcTemplate.queryForList(getGussers, year.value, year.value, year.value).stream()
                .map(row -> (UUID) row.get("id"))
                .map(this::getUserFromId)
                .toList();
    }

    /**
     * Gets every race id from a year where there has been a a race.
     *
     * @param year of season
     * @return id of races
     */
    public List<RaceId> getRaceIdsFinished(Year year) {
        final String getRaceIds = """
                SELECT DISTINCT ro.race_id as race_id, ro.position
                FROM race_order ro
                JOIN race_results rr ON ro.race_id = rr.race_id
                WHERE ro.year = ?
                ORDER BY ro.position;
                """;
        return jdbcTemplate.queryForList(getRaceIds, year.value).stream()
                .map(row -> (int) row.get("race_id"))
                .map(RaceId::new)
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
                SELECT race_id, year, position
                FROM race_order
                WHERE race_id NOT IN (SELECT race_id FROM race_results)
                AND year NOT IN (SELECT year FROM years_finished)
                ORDER BY year, position;
                """;

        return jdbcTemplate.queryForList(sql).stream()
                .map(row -> new CutoffRace(
                        (int) row.get("position"),
                        new RaceId((int) row.get("race_id")),
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
                SELECT ro.race_id
                FROM starting_grids sg
                JOIN race_order ro on ro.race_id = sg.race_id
                WHERE ro.year = ?
                ORDER BY ro.position DESC
                LIMIT 1;
                """;
        return new RaceId(jdbcTemplate.queryForObject(getStartingGridId, Integer.class, year.value));
    }

    /**
     * Gets the id of the latest race result of a season.
     *
     * @param year of season
     * @return race id
     */
    public RaceId getLatestRaceResultId(Year year) {
        final String getRaceResultId = """
                SELECT ro.race_id
                FROM race_results rr
                JOIN race_order ro on ro.race_id = rr.race_id
                WHERE ro.year = ?
                ORDER BY ro.position DESC
                LIMIT 1;
                """;
        return new RaceId(jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year.value));
    }

    public RaceId getUpcomingRaceId(Year year) {
        final String sql = """
                SELECT race_id
                FROM race_order
                WHERE race_id NOT IN (SELECT DISTINCT race_id FROM race_results)
                AND year = ?
                ORDER BY position
                LIMIT 1;
                """;
        return new RaceId(jdbcTemplate.queryForObject(sql, Integer.class, year.value));
    }

    /**
     * Gets the id of the latest race result of a season.
     *
     * @param year of season
     * @return race id
     */
    public RaceId getLatestStandingsId(Year year) {
        final String getRaceResultId = """
                SELECT ro.race_id
                FROM race_order ro
                JOIN driver_standings ds on ds.race_id = ro.race_id
                JOIN constructor_standings cs on cs.race_id = ro.race_id
                WHERE ro.year = ?
                ORDER BY ro.position DESC
                LIMIT 1;
                """;
        return new RaceId(jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year.value));
    }

    /**
     * Checks if starting grid for race already exists.
     *
     * @param raceId to check
     * @return true if exists
     */
    public boolean isStartingGridAdded(RaceId raceId) {
        final String existCheck = "SELECT COUNT(*) FROM starting_grids WHERE race_id = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, raceId.value) > 0;
    }

    /**
     * Checks if race result for race already exists.
     *
     * @param raceId to check
     * @return true if exists
     */
    public boolean isRaceResultAdded(RaceId raceId) {
        final String existCheck = "SELECT COUNT(*) FROM race_results WHERE race_id = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, raceId.value) > 0;
    }

    /**
     * Checks if race already exists.
     *
     * @param raceId to check
     * @return true if exists
     */
    public boolean isRaceAdded(int raceId) {
        final String existCheck = "SELECT COUNT(*) FROM races WHERE race_id = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
    }

    /**
     * Adds name of driver to the Driver table in database.
     *
     * @param driver name
     */
    public void addDriver(String driver) {
        final String insertDriver = "INSERT INTO drivers (driver_name) VALUES (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(insertDriver, driver);
    }

    /**
     * Appends the driver to DriverYear table in the given year.
     *
     * @param driver name
     * @param year   of season
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
        final String getMaxPos = "SELECT COALESCE(MAX(position), 0)::INTEGER FROM drivers_year WHERE year = ?;";
        return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year.value);
    }

    /**
     * Adds driver to the DriverYear table in the given year and position.
     *
     * @param driver   name
     * @param year     of season
     * @param position of driver
     */
    public void addDriverYear(Driver driver, Year year, int position) {
        final String addDriverYear = "INSERT INTO drivers_year (driver_name, year, position) VALUES (?, ?, ?);";
        jdbcTemplate.update(addDriverYear, driver.value, year.value, position);
    }

    /**
     * Removes driver from DriverYear table.
     *
     * @param driver to delete
     * @param year   of season
     */
    public void deleteDriverYear(Driver driver, Year year) {
        final String deleteDriver = "DELETE FROM drivers_year WHERE year = ? AND driver_name = ?;";
        jdbcTemplate.update(deleteDriver, year.value, driver.value);
    }

    /**
     * Removes all drivers from DriverYear table in the given year.
     *
     * @param year of season
     */
    public void deleteAllDriverYear(Year year) {
        final String deleteAllDrivers = "DELETE FROM drivers_year WHERE year = ?;";
        jdbcTemplate.update(deleteAllDrivers, year.value);
    }

    /**
     * Adds name of constructor to the Constructor table in database.
     *
     * @param constructor name
     */
    public void addConstructor(String constructor) {
        final String insertConstructor = "INSERT INTO constructors (constructor_name) VALUES (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(insertConstructor, constructor);
    }

    /**
     * Appends the constructor to ConstructorYear table in the given year.
     *
     * @param constructor name
     * @param year        of season
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
        final String getMaxPos = "SELECT COALESCE(MAX(position), 0)::INTEGER FROM constructors_year WHERE year = ?;";
        return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year.value);
    }

    /**
     * Adds constructor to the ConstructorYear table in the given year and position.
     *
     * @param constructor name
     * @param year        of season
     * @param position    of constructor
     */
    public void addConstructorYear(Constructor constructor, Year year, int position) {
        final String addConstructorYear = "INSERT INTO public.constructors_year (constructor_name, year, position) VALUES (?, ?, ?);";
        jdbcTemplate.update(addConstructorYear, constructor.value, year.value, position);
    }

    /**
     * Removes constructor from ConstructorYear table.
     *
     * @param constructor to delete
     * @param year        of season
     */
    public void deleteConstructorYear(Constructor constructor, Year year) {
        final String deleteConstructor = "DELETE FROM constructors_year WHERE year = ? AND constructor_name = ?;";
        jdbcTemplate.update(deleteConstructor, year.value, constructor.value);
    }

    /**
     * Removes all constructor from ConstructorYear table in the given year.
     *
     * @param year of season
     */
    public void deleteAllConstructorYear(Year year) {
        final String deleteAllConstructors = "DELETE FROM constructors_year WHERE year = ?;";
        jdbcTemplate.update(deleteAllConstructors, year.value);
    }

    /**
     * Insert driving to starting grid in given race and position.
     *
     * @param raceId   to add data to
     * @param position of driver
     * @param driver   to add
     */
    public void insertDriverStartingGrid(RaceId raceId, int position, Driver driver) {
        final String insertStartingGrid = """
            INSERT INTO starting_grids (race_id, position, driver_name) VALUES (?, ?, ?)
            ON CONFLICT (race_id, driver_name)
            DO UPDATE SET position = EXCLUDED.position;
        """;
        jdbcTemplate.update(insertStartingGrid, raceId.value, position, driver.value);
    }

    /**
     * Inserts or replaces race result of driver into RaceResult table.
     *
     * @param raceId            of race
     * @param position          of driver
     * @param driver            name
     * @param points            the driver got
     * @param finishingPosition the position that driver finished race in
     */
    public void insertDriverRaceResult(RaceId raceId, String position, Driver driver, Points points, int finishingPosition) {
        final String insertRaceResult = """
                INSERT INTO race_results
                (race_id, position, driver_name, points, finishing_position)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (race_id, finishing_position)
                DO UPDATE SET position = EXCLUDED.position,
                              driver_name = EXCLUDED.driver_name,
                              points = EXCLUDED.points;
                """;
        jdbcTemplate.update(insertRaceResult, raceId.value, position, driver.value, points.value, finishingPosition);
    }

    /**
     * Inserts or replaces position in standings of driver into DriverStandings table.
     *
     * @param raceId   of race
     * @param driver   name
     * @param position of driver
     * @param points   of driver
     */
    public void insertDriverIntoStandings(RaceId raceId, Driver driver, int position, Points points) {
        final String insertDriverStandings = """
                INSERT INTO driver_standings
                (race_id, driver_name, position, points)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (race_id, driver_name)
                DO UPDATE SET position = EXCLUDED.position, points = EXCLUDED.points;
                """;
        jdbcTemplate.update(insertDriverStandings, raceId.value, driver.value, position, points.value);
    }

    /**
     * Inserts or replaces position in standings of constructor into ConstructorStandings table.
     *
     * @param raceId      of race
     * @param constructor name
     * @param position    of constructor
     * @param points      of constructor
     */
    public void insertConstructorIntoStandings(RaceId raceId, Constructor constructor, int position, Points points) {
        final String insertConstructorStandings = """
                INSERT INTO constructor_standings
                (race_id, constructor_name, position, points)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (race_id, constructor_name)
                DO UPDATE SET position = EXCLUDED.position, points = EXCLUDED.points;
                """;
        jdbcTemplate.update(insertConstructorStandings, raceId.value, constructor.value, position, points.value);
    }

    /**
     * Inserts race id and name into Race table.
     *
     * @param raceId   of race
     * @param raceName of race
     */
    public void insertRace(int raceId, String raceName) {
        final String insertRaceName = "INSERT INTO races (race_id, race_name) VALUES (?, ?) ON CONFLICT DO NOTHING;";
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
        final String sql = "SELECT MAX(position) FROM race_order WHERE year = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, year.value);
    }

    /**
     * Inserts race into RaceOrder.
     *
     * @param raceId   of race
     * @param year     of season
     * @param position of race
     */
    public void insertRaceOrder(RaceId raceId, Year year, int position) {
        final String insertRaceOrder = """
            INSERT INTO race_order (race_id, year, position)
            VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING;
        """;
        jdbcTemplate.update(insertRaceOrder, raceId.value, year.value, position);
    }

    /**
     * Deletes race from Race table.
     *
     * @param raceId to delete
     */
    public void deleteRace(RaceId raceId) {
        final String deleteRace = "DELETE FROM races WHERE race_id = ?;";
        jdbcTemplate.update(deleteRace, raceId.value);
    }

    /**
     * Checks if a season is a valid season. To be valid, it needs to have atleast one
     * race in the RaceOrder table.
     *
     * @param year of season
     * @return true if season is valid
     */
    public boolean isValidSeason(int year) {
        final String validateSeason = "SELECT COUNT(*) FROM years WHERE year = ?;";
        return jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
    }

    /**
     * Checks if a race is a valid race within a season. To be valid, it needs to have a
     * table row in RaceOrder where both year and id are equal to input values.
     *
     * @param raceId of race
     * @param year   of season
     * @return true if race is valid
     */
    public boolean isRaceInSeason(RaceId raceId, Year year) {
        final String validateRaceId = "SELECT COUNT(*) FROM race_order WHERE year = ? AND race_id = ?;";
        return jdbcTemplate.queryForObject(validateRaceId, Integer.class, year.value, raceId.value) > 0;
    }

    /**
     * Gets a list of all valid years. I.E. years that are in RaceOrder table.
     * Ordered descendingly.
     *
     * @return valid years
     */
    public List<Year> getAllValidYears() {
        final String sql = "SELECT DISTINCT year FROM years ORDER BY year DESC;";
        return jdbcTemplate.queryForList(sql, Integer.class).stream()
                .map(Year::new)
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
        final String getRaceIds = "SELECT race_id FROM race_order WHERE year = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getRaceIds, Integer.class, year.value).stream()
                .map(RaceId::new)
                .toList();
    }

    /**
     * Removes all races from RaceOrder in the given season.
     *
     * @param year of season
     */
    public void removeRaceOrderFromSeason(Year year) {
        final String removeOldOrderSql = "DELETE FROM race_order WHERE year = ?;";
        jdbcTemplate.update(removeOldOrderSql, year.value);
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
                SELECT r.race_id as id, r.race_name as name, rc.cutoff as cutoff, ro.year as year, ro.position as position
                FROM race_cutoffs rc
                JOIN race_order ro ON ro.race_id = rc.race_id
                JOIN races r ON ro.race_id = r.race_id
                WHERE ro.year = ?
                ORDER BY ro.position;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getCutoffRaces, year.value);
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

    public List<Race> getRacesYear(Year year) {
        final String getCutoffRaces = """
                SELECT r.race_id as id, r.race_name as name, ro.year as year, ro.position as position
                FROM race_order ro
                JOIN races r ON ro.race_id = r.race_id
                WHERE ro.year = ?
                ORDER BY ro.position;
                """;
        return jdbcTemplate.queryForList(getCutoffRaces, year.value).stream()
                .map(row -> new Race(
                        (int) row.get("position"),
                        (String) row.get("name"),
                        new RaceId((int) row.get("id")),
                        year))
                .toList();
    }

    public List<Race> getRacesYearFinished(Year year) {
        final String getCutoffRaces = """
                SELECT DISTINCT r.race_id as id, r.race_name as name, ro.year as year, ro.position as position
                FROM race_order ro
                JOIN races r ON ro.race_id = r.race_id
                JOIN race_results rr ON rr.race_id = r.race_id
                WHERE ro.year = ?
                ORDER BY ro.position;
                """;
        return jdbcTemplate.queryForList(getCutoffRaces, year.value).stream()
                .map(row -> new Race(
                        (int) row.get("position"),
                        (String) row.get("name"),
                        new RaceId((int) row.get("id")),
                        year))
                .toList();
    }

    public Race getRaceFromId(RaceId raceId) {
        final String sql = """
                SELECT r.race_id as id, r.race_name as name, ro.year as year, ro.position as position
                FROM race_order ro
                JOIN races r ON ro.race_id = r.race_id
                WHERE ro.race_id = ?
                ORDER BY ro.position;
                """;
        Map<String, Object> sqlRes = jdbcTemplate.queryForMap(sql, raceId.value);
        return new Race(
                (int) sqlRes.get("position"),
                (String) sqlRes.get("name"),
                new RaceId((int) sqlRes.get("id")),
                new Year((int) sqlRes.get("year"))
        );
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
     * @param raceId     of race
     */
    public void setCutoffRace(Instant cutoffTime, RaceId raceId) {
        final String setCutoffTime = """
            INSERT INTO race_cutoffs (race_id, cutoff)
            VALUES (?, ?)
            ON CONFLICT (race_id)
            DO UPDATE SET cutoff = EXCLUDED.cutoff;
        """;
        jdbcTemplate.update(setCutoffTime, raceId.value, cutoffTime.toString());
    }

    /**
     * Sets the cutoff of the given season to the given time.
     *
     * @param cutoffTime for guessing
     * @param year       of season
     */
    public void setCutoffYear(Instant cutoffTime, Year year) {
        final String setCutoffTime = """
            INSERT INTO year_cutoffs (year, cutoff) VALUES (?, ?)
            ON CONFLICT (year)
            DO UPDATE SET cutoff = EXCLUDED.cutoff;
        """;
        jdbcTemplate.update(setCutoffTime, year.value, cutoffTime.toString());
    }

    /**
     * Gets a list of registered flags for a given race.
     *
     * @param raceId of race
     * @return registered flags
     */
    public List<RegisteredFlag> getRegisteredFlags(RaceId raceId) {
        List<RegisteredFlag> registeredFlags = new ArrayList<>();
        final String getRegisteredFlags = """
                SELECT flag_name, round, flag_id, session_type
                FROM flag_stats
                WHERE race_id = ?
                ORDER BY session_type, round;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRegisteredFlags, raceId.value);
        for (Map<String, Object> row : sqlRes) {
            Flag type = new Flag((String) row.get("flag_name"));
            int round = (int) row.get("round");
            int id = (int) row.get("flag_id");
            SessionType sessionType = new SessionType((String) row.get("session_type"));
            registeredFlags.add(new RegisteredFlag(type, round, id, sessionType));
        }
        return registeredFlags;
    }

    /**
     * Inserts an instance of a recorded flag to the database.
     * IDs are assigned automatically.
     *
     * @param flag   type of flag
     * @param round  the round flag happened in
     * @param raceId of race
     */
    public void insertFlagStats(Flag flag, int round, RaceId raceId, SessionType sessionType) {
        final String sql = "INSERT INTO flag_stats (flag_name, race_id, round, session_type) VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(sql, flag.value, raceId.value, round, sessionType.value);
    }

    /**
     * Deletes a recorded flag by its id.
     *
     * @param flagId of stat
     */
    public void deleteFlagStatsById(int flagId) {
        final String sql = "DELETE FROM flag_stats WHERE flag_id = ?;";
        jdbcTemplate.update(sql, flagId);
    }

    /**
     * Gets the name of a race.
     *
     * @param raceId of race
     * @return name of race
     */
    public String getRaceName(RaceId raceId) {
        final String getRaceNameSql = "SELECT race_name FROM races WHERE race_id = ?;";
        return jdbcTemplate.queryForObject(getRaceNameSql, String.class, raceId.value);
    }

    /**
     * Gets a list of all the types of flags.
     *
     * @return name of flag types
     */
    public List<Flag> getFlags() {
        final String sql = "SELECT flag_name FROM flags;";
        return jdbcTemplate.queryForList(sql, String.class).stream()
                .map(Flag::new)
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
                SELECT position, driver_name
                FROM starting_grids
                WHERE race_id = ?
                ORDER BY position;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getStartingGrid, raceId.value);
        List<PositionedCompetitor> startingGrid = new ArrayList<>();
        for (Map<String, Object> row : sqlRes) {
            String position = String.valueOf((int) row.get("position"));
            String driver = (String) row.get("driver_name");
            startingGrid.add(new PositionedCompetitor(position, driver, 0));
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
                SELECT position, driver_name, points
                FROM race_results
                WHERE race_id = ?
                ORDER BY finishing_position;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRaceResult, raceId.value);
        List<PositionedCompetitor> raceResult = new ArrayList<>();
        for (Map<String, Object> row : sqlRes) {
            String position = (String) row.get("position");
            String driver = (String) row.get("driver_name");
            int points = (int) row.get("points");
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
                SELECT position, driver_name, points
                FROM driver_standings
                WHERE race_id = ?
                ORDER BY position;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getDriverStandings, raceId.value);
        List<PositionedCompetitor> standings = new ArrayList<>();
        for (Map<String, Object> row : sqlRes) {
            String position = String.valueOf((int) row.get("position"));
            String driver = (String) row.get("driver_name");
            int points = (int) row.get("points");
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
                SELECT position, constructor_name, points
                FROM constructor_standings
                WHERE race_id = ?
                ORDER BY position;
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getConstructorStandings, raceId.value);
        List<PositionedCompetitor> standings = new ArrayList<>();
        for (Map<String, Object> row : sqlRes) {
            String position = String.valueOf((int) row.get("position"));
            String constructor = (String) row.get("constructor_name");
            int points = (int) row.get("points");
            standings.add(new PositionedCompetitor(position, constructor, points));
        }
        return standings;
    }

    /**
     * Checks if a driver is in DriverYear in the given season.
     *
     * @param driver the name of the driver
     * @param year   of season
     * @return true if driver is valid
     */
    public boolean isValidDriverYear(Driver driver, Year year) {
        final String existCheck = "SELECT COUNT(*) FROM drivers_year WHERE year = ? AND driver_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, year.value, driver.value) > 0;
    }

    public boolean isValidDriver(Driver driver) {
        final String existCheck = "SELECT COUNT(*) FROM drivers WHERE driver_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, driver.value) > 0;
    }

    /**
     * Checks if a constructor is in ConstructorYear in the given season.
     *
     * @param constructor the name of the constructor
     * @param year        of season
     * @return true if constructor is valid
     */
    public boolean isValidConstructorYear(Constructor constructor, Year year) {
        final String existCheck = "SELECT COUNT(*) FROM constructors_year WHERE year = ? AND constructor_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, year.value, constructor.value) > 0;
    }

    public boolean isValidConstructor(Constructor constructor) {
        final String existCheck = "SELECT COUNT(*) FROM constructors WHERE constructor_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, constructor.value) > 0;
    }

    public boolean isValidFlag(String value) {
        final String existCheck = "SELECT COUNT(*) FROM flags WHERE flag_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, value) > 0;
    }

    private void addToMailingList(UUID userId, String email) {
        final String sql = """
            INSERT INTO mailing_list (user_id, email)
            VALUES (?, ?)
            ON CONFLICT (user_id)
            DO UPDATE SET email = EXCLUDED.email;
        """;
        jdbcTemplate.update(sql, userId, email);
        removeVerificationCode(userId);
    }

    public void clearUserFromMailing(UUID userId) {
        clearMailPreferences(userId);
        clearNotified(userId);
        deleteUserFromMailingList(userId);
    }

    private void deleteUserFromMailingList(UUID userId) {
        final String sql = "DELETE FROM mailing_list WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }


    public boolean userHasEmail(UUID userId) {
        final String sql = "SELECT COUNT(*) FROM mailing_list WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
    }

    public String getEmail(UUID userId) {
        try {
            final String sql = "SELECT email FROM mailing_list WHERE user_id = ?;";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<UserMail> getMailingList(RaceId raceId) {
        final String sql = """
                SELECT u.google_id as google_id, u.user_id as id, u.username as username, ml.email as email
                FROM users u
                JOIN mailing_list ml ON ml.user_id = u.user_id
                WHERE u.user_id NOT IN
                      (SELECT user_id FROM driver_place_guesses WHERE race_id = ? GROUP BY user_id HAVING COUNT(*) == 2);
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, raceId.value);
        return sqlRes.stream()
                .map(row ->
                        new UserMail(
                                new User(
                                        (String) row.get("google_id"),
                                        (UUID) row.get("id"),
                                        row.get("username").toString())
                                , (String) row.get("email"))
                )
                .toList();
    }

    public void setNotified(RaceId raceId, UUID userId) {
        final String sql = "INSERT INTO notified (user_id, race_id) VALUES (?, ?);";
        jdbcTemplate.update(sql, userId, raceId.value);
    }

    public int getNotifiedCount(RaceId raceId, UUID userId) {
        final String sql = "SELECT COUNT(*) FROM notified WHERE user_id = ? AND race_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, raceId.value);
    }

    private void clearNotified(UUID userId) {
        final String sql = "DELETE FROM notified WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }

    public void addVerificationCode(UserMail userMail, int verificationCode) {
        final String sql = """
                INSERT INTO verification_codes
                (user_id, verification_code, email, cutoff)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id)
                DO UPDATE SET verification_code = EXCLUDED.verification_code,
                              email = EXCLUDED.email,
                              cutoff = EXCLUDED.cutoff;
                """;
        jdbcTemplate.update(sql, userMail.user().id(), verificationCode, userMail.email(), Instant.now().plus(Duration.ofMinutes(10)).toString());
    }

    public void removeVerificationCode(UUID userId) {
        final String sql = "DELETE FROM verification_codes WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void removeExpiredVerificationCodes() {
        final String sql = "SELECT cutoff, user_id FROM verification_codes;";
        List<UUID> expired = getExpiredCodes(sql);
        for (UUID userId : expired) {
            removeVerificationCode(userId);
        }
    }

    public boolean hasVerificationCode(UUID userId) {
        final String sql = "SELECT COUNT(*) FROM verification_codes WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
    }

    public boolean isValidVerificationCode(UUID userId, int verificationCode) {
        final String sql = "SELECT COUNT(*) FROM verification_codes WHERE user_id = ? AND verification_code = ?;";
        boolean isValidCode = jdbcTemplate.queryForObject(sql, Integer.class, userId, verificationCode) > 0;
        if (!isValidCode) {
            return false;
        }
        final String getCutoffSql = "SELECT cutoff FROM verification_codes WHERE user_id = ?;";
        Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoffSql, String.class, userId));
        boolean isValidCutoff = cutoff.compareTo(Instant.now()) > 0;
        if (!isValidCutoff) {
            return false;
        }
        final String emailSql = "SELECT email FROM verification_codes WHERE user_id = ?;";
        String email = jdbcTemplate.queryForObject(emailSql, String.class, userId);
        addToMailingList(userId, email);
        return true;
    }

    public boolean isValidReferralCode(long referralCode) {
        final String sql = "SELECT COUNT(*) FROM referral_codes WHERE referral_code = ?;";
        boolean isValidCode = jdbcTemplate.queryForObject(sql, Integer.class, referralCode) > 0;
        if (!isValidCode) {
            return false;
        }
        final String getCutoffSql = "SELECT cutoff FROM referral_codes WHERE referral_code = ?;";
        Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoffSql, String.class, referralCode));
        return cutoff.compareTo(Instant.now()) > 0;
    }

    public void removeExpiredReferralCodes() {
        final String getExpiredSql = "SELECT cutoff, user_id FROM referral_codes;";
        List<UUID> expired = getExpiredCodes(getExpiredSql);
        final String removeSql = "DELETE FROM referral_codes WHERE user_id = ?;";
        for (UUID userId : expired) {
            jdbcTemplate.update(removeSql, userId);
        }
    }

    private List<UUID> getExpiredCodes(final String sql) {
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql);
        return sqlRes.stream()
                .filter(row -> Instant.parse((String) row.get("cutoff")).compareTo(Instant.now()) < 0)
                .map(row -> (UUID) row.get("user_id"))
                .toList();
    }

    public long addReferralCode(UUID userId) {
        final String sql = """
                INSERT INTO referral_codes
                (user_id, referral_code, cutoff)
                VALUES (?, ?, ?)
                ON CONFLICT (user_id)
                DO UPDATE SET referral_code = EXCLUDED.referral_code,
                              cutoff = EXCLUDED.cutoff;
                """;
        long referralCode = CodeGenerator.getReferralCode();
        Instant cutoff = Instant.now().plus(Duration.ofHours(1));
        jdbcTemplate.update(sql, userId, referralCode, cutoff.toString());
        return referralCode;
    }

    public Long getReferralCode(UUID userId) {
        final String sql = "SELECT referral_code FROM referral_codes WHERE user_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<CompetitorGuessYear<Driver>> userGuessDataDriver(UUID userId) {
        final String sql = """
                SELECT position, driver_name, year
                FROM driver_guesses
                WHERE user_id = ?
                ORDER BY year DESC, position;
                """;
        return jdbcTemplate.queryForList(sql, userId).stream()
                .map(row ->
                        new CompetitorGuessYear<>(
                                (int) row.get("position"),
                                new Driver((String) row.get("driver_name")),
                                new Year((int) row.get("year"))
                        )).toList();
    }

    public List<CompetitorGuessYear<Constructor>> userGuessDataConstructor(UUID userId) {
        final String sql = """
                SELECT position, constructor_name, year
                FROM constructor_guesses
                WHERE user_id = ?
                ORDER BY year DESC, position;
                """;
        return jdbcTemplate.queryForList(sql, userId).stream()
                .map(row ->
                        new CompetitorGuessYear<>(
                                (int) row.get("position"),
                                new Constructor((String) row.get("constructor_name")),
                                new Year((int) row.get("year"))
                        )).toList();
    }

    public List<FlagGuessYear> userGuessDataFlag(UUID userId) {
        final String sql = """
                SELECT flag_name, amount, year
                FROM flag_guesses
                WHERE user_id = ?
                ORDER BY year DESC, flag_name;
                """;
        return jdbcTemplate.queryForList(sql, userId).stream()
                .map(row -> new FlagGuessYear(
                        new Flag((String) row.get("flag_name")),
                        (int) row.get("amount"),
                        new Year((int) row.get("year"))
                )).toList();

    }

    public List<PlaceGuess> userGuessDataDriverPlace(UUID userId) {
        final String sql = """
                SELECT dpg.category_name AS category, dpg.driver_name AS driver, r.race_name AS race_name, ro.year AS year
                FROM driver_place_guesses dpg
                JOIN races r ON dpg.race_id = r.race_id
                JOIN race_order ro ON dpg.race_id = ro.race_id
                WHERE dpg.user_id = ?
                ORDER BY ro.year DESC, ro.position, dpg.category_name;
                """;
        return jdbcTemplate.queryForList(sql, userId).stream()
                .map(row -> new PlaceGuess(
                        new Category((String) row.get("category")),
                        new Driver((String) row.get("driver")),
                        (String) row.get("race_name"),
                        new Year((int) row.get("year"))
                )).toList();
    }

    public List<UserNotifiedCount> userDataNotified(UUID userId) {
        final String sql = """
                SELECT r.race_name AS name, count(*)::INTEGER as notified_count, ro.year AS year
                FROM notified n
                JOIN races r ON n.race_id = r.race_id
                JOIN race_order ro ON n.race_id = ro.race_id
                WHERE n.user_id = ?
                GROUP BY n.race_id, ro.position, ro.year, r.race_name
                ORDER BY ro.year DESC, ro.position;
                """;
        return jdbcTemplate.queryForList(sql, userId).stream()
                .map(row -> new UserNotifiedCount(
                        (String) row.get("name"),
                        (int) row.get("notified_count"),
                        new Year((int) row.get("year"))
                )).toList();
    }

    public boolean isValidMailOption(int option) {
        final String sql = "SELECT COUNT(*) FROM mail_options WHERE mail_option = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, option) > 0;
    }

    public void addMailOption(UUID userId, MailOption option) {
        final String sql = "INSERT INTO mail_preferences (user_id, mail_option) VALUES (?, ?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(sql, userId, option.value);
    }

    public void removeMailOption(UUID userId, MailOption option) {
        final String sql = "DELETE FROM mail_preferences WHERE user_id = ? AND mail_option = ?;";
        jdbcTemplate.update(sql, userId, option.value);
    }

    private void clearMailPreferences(UUID userId) {
        final String sql = "DELETE FROM mail_preferences WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }

    public List<MailOption> getMailingPreference(UUID userId) {
        final String sql = "SELECT mail_option FROM mail_preferences WHERE user_id = ? ORDER BY mail_option DESC;";
        return jdbcTemplate.queryForList(sql, Integer.class, userId).stream()
                .map(MailOption::new)
                .toList();
    }

    public List<MailOption> getMailingOptions() {
        final String sql = "SELECT mail_option FROM mail_options ORDER BY mail_option;";
        return jdbcTemplate.queryForList(sql, Integer.class).stream()
                .map(MailOption::new)
                .toList();
    }

    public void setTeamDriver(Driver driver, Constructor team, Year year) {
        final String sql = """
            INSERT INTO drivers_team (driver_name, team, year)
            VALUES (?, ?, ?)
            ON CONFLICT (driver_name, year)
            DO UPDATE SET team = EXCLUDED.team;
        """;
        jdbcTemplate.update(sql, driver.value, team.value, year.value);
    }

    public void addColorConstructor(Constructor constructor, Year year, Color color) {
        if (color.value() == null) {
            return;
        }
        final String sql = """
            INSERT INTO constructors_color (constructor_name, year, color)
            VALUES (?, ?, ?)
            ON CONFLICT (constructor_name, year)
            DO UPDATE SET color = EXCLUDED.color;
        """;
        jdbcTemplate.update(sql, constructor.value, year.value, color.value());
    }

    public List<ColoredCompetitor<Constructor>> getConstructorsYearWithColors(Year year) {
        final String sql = """
                SELECT cy.constructor_name as constructor, cc.color as color
                FROM constructors_year cy
                LEFT JOIN constructors_color cc ON cc.constructor_name = cy.constructor_name
                WHERE cy.year = ?
                ORDER BY cy.position;
                """;
        return jdbcTemplate.queryForList(sql, year.value).stream()
                .map(row -> new ColoredCompetitor<>(
                        new Constructor((String) row.get("constructor")),
                        new Color((String) row.get("color"))))
                .toList();
    }

    public List<ValuedCompetitor<Driver, Constructor>> getDriversTeam(Year year) {
        final String sql = """
                SELECT dy.driver_name as driver, dt.team as team
                FROM drivers_year dy
                LEFT JOIN drivers_team dt ON dt.driver_name = dy.driver_name
                WHERE dy.year = ?
                ORDER BY dy.position;
                """;
        return jdbcTemplate.queryForList(sql, year.value).stream()
                .map(row -> new ValuedCompetitor<>(
                        new Driver((String) row.get("driver")),
                        new Constructor((String) row.get("team"))))
                .toList();
    }


    public void addBingomaster(UUID userId) {
        final String sql = "INSERT INTO bingomasters (user_id) VALUES (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(sql, userId);
    }

    public void removeBingomaster(UUID userId) {
        final String sql = "DELETE FROM bingomasters WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }

    public List<User> getBingomasters() {
        final String getAllUsersSql = """
                SELECT u.user_id AS id, u.username AS username, u.google_id AS google_id
                FROM users u
                JOIN bingomasters bm ON u.user_id = bm.user_id
                ORDER BY u.username;
                """;
        return jdbcTemplate.queryForList(getAllUsersSql).stream()
                .map(row ->
                        new User(
                                (String) row.get("google_id"),
                                (UUID) row.get("id"),
                                row.get("username").toString())
                ).toList();
    }

    public boolean isBingomaster(UUID userId) {
        final String sql = "SELECT COUNT(*) FROM bingomasters WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
    }

    public List<BingoSquare> getBingoCard(Year year) {
        final String sql = """
                SELECT year, bingo_square_id, square_text, marked
                FROM bingo_cards
                WHERE year = ?
                ORDER BY bingo_square_id;
                """;
        return jdbcTemplate.queryForList(sql, year.value).stream()
                .map(row ->
                        new BingoSquare(
                                (String) row.get("square_text"),
                                (boolean) row.get("marked"),
                                (int) row.get("bingo_square_id"),
                                new Year((int) row.get("year"))
                        )
                ).toList();
    }

    public BingoSquare getBingoSquare(Year year, int id) {
        final String sql = """
                SELECT year, bingo_square_id, square_text, marked
                FROM bingo_cards
                WHERE year = ? AND bingo_square_id = ?;
                """;
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, year.value, id);
        return new BingoSquare(
                (String) row.get("square_text"),
                (boolean) row.get("marked"),
                (int) row.get("bingo_square_id"),
                new Year((int) row.get("year"))
        );
    }

    public void addBingoSquare(BingoSquare bingoSquare) {
        final String sql = """
                INSERT INTO bingo_cards
                (year, bingo_square_id, square_text, marked)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (year, bingo_square_id)
                DO UPDATE SET square_text = EXCLUDED.square_text, marked = EXCLUDED.marked;
                """;
        jdbcTemplate.update(
                sql,
                bingoSquare.year().value,
                bingoSquare.id(),
                bingoSquare.text(),
                bingoSquare.marked()
        );
    }

    public void toogleMarkBingoSquare(Year year, int id) {
        BingoSquare bingoSquare = getBingoSquare(year, id);
        boolean newMark = !bingoSquare.marked();
        final String sql = """
                UPDATE bingo_cards
                SET marked = ?
                WHERE year = ? AND bingo_square_id = ?;
                """;
        jdbcTemplate.update(sql, newMark, year.value, id);
    }

    public void setTextBingoSquare(Year year, int id, String text) {
        final String sql = """
                UPDATE bingo_cards
                SET square_text = ?
                WHERE year = ? AND bingo_square_id = ?;
                """;
        jdbcTemplate.update(sql, text, year.value, id);
    }

    public boolean isBingoCardAdded(Year year) {
        final String sql = """
                SELECT COUNT(*)
                FROM bingo_cards
                WHERE year = ?;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, year.value) > 0;
    }

    public boolean isValidSessionType(String sessionType) {
        final String sql = "SELECT COUNT(*) FROM session_types WHERE session_type = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, sessionType) > 0;
    }

    public List<SessionType> getSessionTypes() {
        final String sql = "SELECT session_type FROM session_types ORDER BY session_type;";
        return jdbcTemplate.queryForList(sql).stream()
                .map(row -> new SessionType((String) row.get("session_type")))
                .toList();
    }

    public String getAlternativeDriverName(String driver, Year year) {
        final String sql = """
                SELECT driver_name
                FROM drivers_alternative_name
                WHERE alternative_name = ? AND year = ?;
                """;
        try {
            return jdbcTemplate.queryForObject(sql, String.class, driver, year.value);
        } catch (EmptyResultDataAccessException e) {
            return driver;
        }
    }

    public Map<String, String> getAlternativeDriverNamesYear(Year year) {
        final String sql = """
                SELECT driver_name, alternative_name
                FROM drivers_alternative_name
                WHERE year = ?;
                """;
        Map<String, String> linkedMap = new LinkedHashMap<>();
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, year.value);
        for (Map<String, Object> row : sqlRes) {
            String driverName = (String) row.get("driver_name");
            String alternativeName = (String) row.get("alternative_name");
            linkedMap.put(alternativeName, driverName);
        }
        return linkedMap;
    }

    public String getAlternativeDriverName(String driver, RaceId raceId) {
        Year year = getYearFromRaceId(raceId);
        return getAlternativeDriverName(driver, year);
    }

    public Year getYearFromRaceId(RaceId raceId) {
        final String sql = "SELECT year FROM race_order WHERE race_id = ?;";
        return new Year(jdbcTemplate.queryForObject(sql, Integer.class, raceId.value));
    }

    public void addAlternativeDriverName(Driver driver, String alternativeName, Year year) {
        final String sql = """
                INSERT INTO drivers_alternative_name
                (driver_name, alternative_name, year)
                VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING;
                """;
        jdbcTemplate.update(sql, driver.value, alternativeName, year.value);
    }

    public void deleteAlternativeName(Driver driver, Year year, String alternativeName) {
        final String sql = "DELETE FROM drivers_alternative_name WHERE driver_name = ? AND year = ? AND alternative_name = ?;";
        jdbcTemplate.update(sql, driver.value, year.value, alternativeName);
    }

    public List<UUID> getAdmins() {
        final String sql = "SELECT user_id FROM admins;";
        return jdbcTemplate.queryForList(sql, String.class).stream()
                .map(UUID::fromString)
                .toList();
    }

    public List<String> getUnregisteredUsers() {
        final String sql = """
                	SELECT SESSION_ID, LAST_ACCESS_TIME
                	FROM SPRING_SESSION
                	WHERE PRINCIPAL_NAME NOT IN (
                		SELECT google_id from users
                	);
                """;
        List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql);
        return sqlRes.stream()
                .filter(session -> {
                    long lastAccess = (long) session.get("LAST_ACCESS_TIME");
                    long now = System.currentTimeMillis();
                    long diff = now - lastAccess;
                    long hourMillis = 3600000;
                    return diff >= hourMillis;
                })
                .map(session -> (String) session.get("SESSION_ID"))
                .toList();
    }

    public void addYear(int year) {
        final String sql = "INSERT INTO years (year) values (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(sql, year);
    }

    public Summary getSummary(RaceId raceId, Year year, PublicUser user) {
        try {
            List<Map<String, Object>> categoriesRes;
            Map<String, Object> totalRes;
            if (raceId != null) {
                final String categoriesSql = """
                            SELECT category_name, placement, points
                            FROM placements_category
                            WHERE race_id = ?
                            AND user_id = ?;
                        """;
                final String totalSql = """
                            SELECT placement, points
                            FROM placements_race
                            WHERE race_id = ?
                            AND user_id = ?;
                        """;
                categoriesRes = jdbcTemplate.queryForList(categoriesSql, raceId.value, user.id);
                totalRes = jdbcTemplate.queryForMap(totalSql, raceId.value, user.id);
            } else {
                final String categoriesSql = """
                            SELECT category_name, placement, points
                            FROM placements_category_year_start
                            WHERE year = ?
                            AND user_id = ?;
                        """;
                final String totalSql = """
                            SELECT placement, points
                            FROM placements_race_year_start
                            WHERE year = ?
                            AND user_id = ?;
                        """;
                categoriesRes = jdbcTemplate.queryForList(categoriesSql, year.value, user.id);
                totalRes = jdbcTemplate.queryForMap(totalSql, year.value, user.id);
            }
            Map<Category, Placement<Points>> categories = new HashMap<>();
            for (Map<String, Object> row : categoriesRes) {
                Category category = new Category((String) row.get("category_name"));
                Position pos = new Position((int) row.get("placement"));
                Points points = new Points((int) row.get("points"));
                Placement<Points> placement = new Placement<>(pos, points);
                categories.put(category, placement);
            }
            Placement<Points> drivers = categories.get(new Category("DRIVER", this));
            Placement<Points> constructors = categories.get(new Category("CONSTRUCTOR", this));
            Placement<Points> flag = categories.get(new Category("FLAG", this));
            Placement<Points> winner = categories.get(new Category("FIRST", this));
            Placement<Points> tenth = categories.get(new Category("TENTH", this));
            Placement<Points> total =
                    new Placement<>(new Position((int) totalRes.get("placement")),
                            new Points((int) totalRes.get("points")));
            return new Summary(drivers, constructors, flag, winner, tenth, total);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Placement<Year>> getPreviousPlacements(UUID userId) {
        final String sql = """
            SELECT placement, year
            FROM placements_year
            WHERE user_id = ?
            ORDER BY year DESC;
        """;
        return jdbcTemplate.queryForList(sql, userId).stream()
                .map(row ->
                        new Placement<>(
                                new Position((int) row.get("placement")),
                                new Year((int) row.get("year"))
                        ))
                .toList();
    }

    public Medals getMedals(UUID userId) {
        final String sql = """
            SELECT COUNT(CASE WHEN placement = 1 THEN 1 END)::INTEGER AS gold,
                   COUNT(CASE WHEN placement = 2 THEN 1 END)::INTEGER AS silver,
                   COUNT(CASE WHEN placement = 3 THEN 1 END)::INTEGER AS bronze
            FROM placements_year
            WHERE user_id = ?;
        """;
        Map<String, Object> res = jdbcTemplate.queryForMap(sql, userId);
        return new Medals(
            new MedalCount((int) res.get("gold")),
            new MedalCount((int) res.get("silver")),
            new MedalCount((int) res.get("bronze"))
        );
    }

    public void addUserScore(UUID userId, Summary summary, RaceId raceId, Year year) {
        if (raceId != null) {
            addUserScoreRace(userId, summary, raceId);
        } else {
            addUserScoreYearStart(userId, summary, year);
        }
    }

    private void addUserScoreRace(UUID userId, Summary summary, RaceId raceId) {
        final String addTotalSql = """
            INSERT INTO placements_race
            (race_id, user_id, placement, points)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (race_id, user_id)
            DO UPDATE SET placement = EXCLUDED.placement, points = EXCLUDED.points;
        """;
        final String addCategorySql = """
            INSERT INTO placements_category
            (race_id, user_id, category_name, placement, points)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (race_id, user_id, category_name)
            DO UPDATE SET placement = EXCLUDED.placement, points = EXCLUDED.points;
        """;
        jdbcTemplate.update(addTotalSql, raceId.value, userId, summary.total().pos().value(), summary.total().value().value);
        jdbcTemplate.update(addCategorySql, raceId.value, userId, new Category("DRIVER").value, summary.drivers().pos().value(), summary.drivers().value().value);
        jdbcTemplate.update(addCategorySql, raceId.value, userId, new Category("CONSTRUCTOR").value, summary.constructors().pos().value(), summary.constructors().value().value);
        jdbcTemplate.update(addCategorySql, raceId.value, userId, new Category("FLAG").value, summary.flag().pos().value(), summary.flag().value().value);
        jdbcTemplate.update(addCategorySql, raceId.value, userId, new Category("FIRST").value, summary.winner().pos().value(), summary.winner().value().value);
        jdbcTemplate.update(addCategorySql, raceId.value, userId, new Category("TENTH").value, summary.tenth().pos().value(), summary.tenth().value().value);
    }

    private void addUserScoreYearStart(UUID userId, Summary summary, Year year) {
        final String addTotalSql = """
            INSERT INTO placements_race_year_start
            (year, user_id, placement, points)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (year, user_id)
            DO UPDATE SET placement = EXCLUDED.placement, points = EXCLUDED.points;
        """;
        final String addCategorySql = """
            INSERT INTO placements_category_year_start
            (year, user_id, category_name, placement, points)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (year, user_id, category_name)
            DO UPDATE SET placement = EXCLUDED.placement, points = EXCLUDED.points;
        """;
        jdbcTemplate.update(addTotalSql, year.value, userId, summary.total().pos().value(), summary.total().value().value);
        jdbcTemplate.update(addCategorySql, year.value, userId, new Category("DRIVER").value, summary.drivers().pos().value(), summary.drivers().value().value);
        jdbcTemplate.update(addCategorySql, year.value, userId, new Category("CONSTRUCTOR").value, summary.constructors().pos().value(), summary.constructors().value().value);
        jdbcTemplate.update(addCategorySql, year.value, userId, new Category("FLAG").value, summary.flag().pos().value(), summary.flag().value().value);
        jdbcTemplate.update(addCategorySql, year.value, userId, new Category("FIRST").value, summary.winner().pos().value(), summary.winner().value().value);
        jdbcTemplate.update(addCategorySql, year.value, userId, new Category("TENTH").value, summary.tenth().pos().value(), summary.tenth().value().value);
    }

    public List<GuesserPointsSeason> getGraph(Year year) {
        final String sql = """
            SELECT prys.user_id as guesser, u.username as username, prys.points as points, 0::INTEGER as position
            FROM placements_race_year_start prys
            JOIN users u ON u.user_id = prys.user_id
            WHERE year = ?
            UNION
            (SELECT pr.user_id AS guesser, u.username as username, pr.points AS points, ro.position AS position
            FROM placements_race pr
            JOIN race_order ro ON ro.race_id = pr.race_id
            JOIN users u ON u.user_id = pr.user_id
            WHERE year = ?)
            ORDER BY position;
        """;
        Map<UUID, List<Points>> userPoints = new HashMap<>();
        Map<UUID, String> usernames = new HashMap<>();
        List<Map<String, Object>> res = jdbcTemplate.queryForList(sql, year.value, year.value);
        for (Map<String, Object> row : res) {
            UUID id = (UUID) row.get("guesser");
            Points points = new Points((int) row.get("points"));
            if (!userPoints.containsKey(id)) {
                String username = row.get("username").toString();
                usernames.put(id, username);
                userPoints.put(id, new ArrayList<>());
            }
            userPoints.get(id).add(points);
        }
        return userPoints.entrySet().stream()
                .map(entry -> new GuesserPointsSeason(usernames.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    public List<RankedGuesser> getLeaderboard(Year year) {
        final String maxPosSql = """
            SELECT MAX(position)
            FROM (
                SELECT 0::INTEGER as position
                FROM placements_race_year_start prys
                UNION
                SELECT ro.position as position
                FROM placements_race pr
                JOIN race_order ro ON ro.race_id = pr.race_id
                WHERE ro.year = ?
                GROUP BY ro.position
                ) as positions;""";
        int maxPos = jdbcTemplate.queryForObject(maxPosSql, Integer.class, year.value);
        final String sql = """
            SELECT guesser, username, points, position, placement
            FROM (SELECT prys.user_id as guesser, u.username as username, prys.points as points, 0::INTEGER as position, prys.placement as placement
            FROM placements_race_year_start prys
            JOIN users u ON u.user_id = prys.user_id
            WHERE year = ?
            UNION
            (SELECT pr.user_id AS guesser, u.username as username, pr.points AS points, ro.position AS position, pr.placement as placement
            FROM placements_race pr
            JOIN race_order ro ON ro.race_id = pr.race_id
            JOIN users u ON u.user_id = pr.user_id
            WHERE year = ?)) as placements_race
            WHERE position = ?
            ORDER BY placement, username;
        """;
        List<Map<String, Object>> res = jdbcTemplate.queryForList(sql, year.value, year.value, maxPos);
        return res.stream()
                .map(row -> new RankedGuesser(
                        new Guesser(
                        row.get("username").toString(),
                        new Points((int) row.get("points")),
                                (UUID) row.get("guesser")
                ), new Position((int) row.get("placement"))
                ))
                .toList();
    }

    public void finalizeYear(Year year) {
        if (isFinishedYear(year)) {
            return;
        }
        final String markAsFinished = "INSERT INTO years_finished (year) VALUES (?);";
        jdbcTemplate.update(markAsFinished, year.value);
        final String addPlacement = "INSERT INTO placements_year (year, user_id, placement) VALUES (?, ?, ?);";
        List<RankedGuesser> leaderboard = getLeaderboard(year);
        for (RankedGuesser rankedGuesser : leaderboard) {
            jdbcTemplate.update(addPlacement, year.value, rankedGuesser.guesser().id(), rankedGuesser.rank().value());
        }
    }

    public boolean isFinishedYear(Year year) {
        final String sql = "SELECT COUNT(*) FROM years_finished WHERE year = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, year.value) > 0;
    }

    public Year getYearFromFlagId(int id) {
        final String sql = """
            SELECT ro.year
            FROM race_order ro
            JOIN flag_stats fs ON ro.race_id = fs.race_id
            WHERE fs.flag_id = ?;
        """;
        return new Year(jdbcTemplate.queryForObject(sql, Integer.class, id));
    }

    public void removeReferralCode(UUID userId) {
        final String sql = "DELETE FROM referral_codes WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }
}
