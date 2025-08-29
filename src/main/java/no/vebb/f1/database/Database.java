package no.vebb.f1.database;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.user.UserRespository;
import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.year.YearService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.*;
import no.vebb.f1.util.collection.*;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserMail;

@Service
@SuppressWarnings("DataFlowIssue")
public class Database {

    private final JdbcTemplate jdbcTemplate;
    private final UserRespository userRespository;
    private final YearService yearService;
    private final RaceService raceService;

    public Database(JdbcTemplate jdbcTemplate, UserRespository userRespository, YearService yearService, RaceService raceService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRespository = userRespository;
        this.yearService = yearService;
        this.raceService = raceService;
    }

    public Instant getCutoffYear(Year year) throws EmptyResultDataAccessException {
        final String getCutoff = "SELECT cutoff FROM year_cutoffs WHERE year = ?;";
        return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, year.value));
    }

    public Instant getCutoffRace(RaceId raceId) throws NoAvailableRaceException {
        try {
            final String getCutoff = "SELECT cutoff FROM race_cutoffs WHERE race_id = ?;";
            return Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceId.value));
        } catch (EmptyResultDataAccessException e) {
            throw new NoAvailableRaceException("There is no cutoff for the given raceId '" + raceId + "'");
        }
    }

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

    public List<Driver> getGuessedYearDriver(Year year, UUID userId) {
        final String guessedSql = "SELECT driver_name FROM driver_guesses WHERE year = ?  AND user_id = ? ORDER BY position";
        return jdbcTemplate.queryForList(guessedSql, String.class, year.value, userId).stream()
                .map(Driver::new)
                .toList();
    }

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

    public long getTimeLeftToGuessRace(RaceId raceId) {
        Instant now = Instant.now();
        final String getCutoff = "SELECT cutoff FROM race_cutoffs WHERE race_id = ?;";
        Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, raceId.value));
        return Duration.between(now, cutoff).toSeconds();
    }

    public int getTimeLeftToGuessRaceHours(RaceId raceId) {
        return (int) (getTimeLeftToGuessRace(raceId) / 3600L);
    }

    public long getTimeLeftToGuessYear() {
        Instant now = Instant.now();
        final String getCutoff = "SELECT cutoff FROM year_cutoffs WHERE year = ?;";
        Instant cutoffYear = Instant
                .parse(jdbcTemplate.queryForObject(getCutoff, String.class, TimeUtil.getCurrentYear()));
        return Duration.between(now, cutoffYear).toSeconds();
    }

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

    public Driver getGuessedDriverPlace(RaceId raceId, Category category, UUID userId) {
        final String getPreviousGuessSql = """
                SELECT driver_name
                FROM driver_place_guesses
                WHERE race_id = ? AND category_name = ? AND user_id = ?;
                """;
        return new Driver(jdbcTemplate.queryForObject(getPreviousGuessSql, String.class, raceId.value, category.value, userId));
    }

    public void addDriverPlaceGuess(UUID userId, RaceId raceId, Driver driver, Category category) {
        final String insertGuessSql = """
            INSERT INTO driver_place_guesses (user_id, race_id, driver_name, category_name)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, race_id, category_name)
            DO UPDATE SET driver_name = EXCLUDED.driver_name;
        """;
        jdbcTemplate.update(insertGuessSql, userId, raceId.value, driver.value, category.value);
    }

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

    public List<Driver> getDriversYear(Year year) {
        final String getDriversSql = "SELECT driver_name FROM drivers_year WHERE year = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getDriversSql, String.class, year.value).stream()
                .map(Driver::new)
                .toList();
    }

    public List<Constructor> getConstructorsYear(Year year) {
        final String getConstructorSql = "SELECT constructor_name FROM constructors_year WHERE year = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getConstructorSql, String.class, year.value).stream()
                .map(Constructor::new)
                .toList();
    }

    public void insertDriversYearGuess(UUID userId, Driver driver, Year year, int position) {
        final String addRowDriver = """
            INSERT INTO driver_guesses (user_id, driver_name, year, position)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, position, year)
            DO UPDATE SET driver_name = EXCLUDED.driver_name;
        """;
        jdbcTemplate.update(addRowDriver, userId, driver.value, year.value, position);
    }

    public void insertConstructorsYearGuess(UUID userId, Constructor constructor, Year year, int position) {
        final String addRowConstructor = """
            INSERT INTO constructor_guesses (user_id, constructor_name, year, position)
            values (?, ?, ?, ?)
            ON CONFLICT (user_id, position, year)
            DO UPDATE SET constructor_name = EXCLUDED.constructor_name;
         """;
        jdbcTemplate.update(addRowConstructor, userId, constructor.value, year.value, position);
    }

    public List<Category> getCategories() {
        final String sql = "SELECT category_name FROM categories;";
        return jdbcTemplate.queryForList(sql, String.class).stream()
                .map(name -> new Category(name, this))
                .toList();
    }

    public boolean isValidCategory(String category) {
        final String validateCategory = "SELECT COUNT(*) FROM categories WHERE category_name = ?;";
        return jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
    }

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

    public Diff getMaxDiffInPointsMap(Year year, Category category) {
        final String getMaxDiff = "SELECT MAX(diff) FROM diff_points_mappings WHERE year = ? AND category_name = ?;";
        return new Diff(jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year.value, category.value));
    }

    public void addDiffToPointsMap(Category category, Diff diff, Year year) {
        final String addDiff = "INSERT INTO diff_points_mappings (category_name, diff, points, year) VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(addDiff, category.value, diff.value, 0, year.value);
    }

    public void removeDiffToPointsMap(Category category, Diff diff, Year year) {
        final String deleteRowWithDiff = "DELETE FROM diff_points_mappings WHERE year = ? AND category_name = ? AND diff = ?;";
        jdbcTemplate.update(deleteRowWithDiff, year.value, category.value, diff.value);
    }

    public void setNewDiffToPointsInPointsMap(Category category, Diff diff, Year year, Points points) {
        final String setNewPoints = """
                UPDATE diff_points_mappings
                SET points = ?
                WHERE diff = ? AND year = ? AND category_name = ?;
                """;
        jdbcTemplate.update(setNewPoints, points.value, diff.value, year.value, category.value);
    }

    public boolean isValidDiffInPointsMap(Category category, Diff diff, Year year) {
        final String validateDiff = "SELECT COUNT(*) FROM diff_points_mappings WHERE year = ? AND category_name = ? AND diff = ?;";
        return jdbcTemplate.queryForObject(validateDiff, Integer.class, year.value, category.value, diff.value) > 0;
    }

    public List<UserEntity> getSeasonGuessers(Year year) {
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
                .map(userRespository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void addDriver(String driver) {
        final String insertDriver = "INSERT INTO drivers (driver_name) VALUES (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(insertDriver, driver);
    }

    public void addDriverYear(String driver, Year year) {
        addDriver(driver);
        int position = getMaxPosDriverYear(year) + 1;
        addDriverYear(new Driver(driver), year, position);
    }

    public int getMaxPosDriverYear(Year year) {
        final String getMaxPos = "SELECT COALESCE(MAX(position), 0)::INTEGER FROM drivers_year WHERE year = ?;";
        return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year.value);
    }

    public void addDriverYear(Driver driver, Year year, int position) {
        final String addDriverYear = "INSERT INTO drivers_year (driver_name, year, position) VALUES (?, ?, ?);";
        jdbcTemplate.update(addDriverYear, driver.value, year.value, position);
    }

    public void deleteDriverYear(Driver driver, Year year) {
        final String deleteDriver = "DELETE FROM drivers_year WHERE year = ? AND driver_name = ?;";
        jdbcTemplate.update(deleteDriver, year.value, driver.value);
    }

    public void deleteAllDriverYear(Year year) {
        final String deleteAllDrivers = "DELETE FROM drivers_year WHERE year = ?;";
        jdbcTemplate.update(deleteAllDrivers, year.value);
    }

    public void addConstructor(String constructor) {
        final String insertConstructor = "INSERT INTO constructors (constructor_name) VALUES (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(insertConstructor, constructor);
    }

    public void addConstructorYear(String constructor, Year year) {
        addConstructor(constructor);
        int position = getMaxPosConstructorYear(year) + 1;
        addConstructorYear(new Constructor(constructor), year, position);
    }

    public int getMaxPosConstructorYear(Year year) {
        final String getMaxPos = "SELECT COALESCE(MAX(position), 0)::INTEGER FROM constructors_year WHERE year = ?;";
        return jdbcTemplate.queryForObject(getMaxPos, Integer.class, year.value);
    }

    public void addConstructorYear(Constructor constructor, Year year, int position) {
        final String addConstructorYear = "INSERT INTO public.constructors_year (constructor_name, year, position) VALUES (?, ?, ?);";
        jdbcTemplate.update(addConstructorYear, constructor.value, year.value, position);
    }

    public void deleteConstructorYear(Constructor constructor, Year year) {
        final String deleteConstructor = "DELETE FROM constructors_year WHERE year = ? AND constructor_name = ?;";
        jdbcTemplate.update(deleteConstructor, year.value, constructor.value);
    }

    public void deleteAllConstructorYear(Year year) {
        final String deleteAllConstructors = "DELETE FROM constructors_year WHERE year = ?;";
        jdbcTemplate.update(deleteAllConstructors, year.value);
    }

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

    public LocalDateTime getCutoffYearLocalTime(Year year) {
        return TimeUtil.instantToLocalTime(getCutoffYear(year));
    }

    public void setCutoffRace(Instant cutoffTime, RaceId raceId) {
        final String setCutoffTime = """
            INSERT INTO race_cutoffs (race_id, cutoff)
            VALUES (?, ?)
            ON CONFLICT (race_id)
            DO UPDATE SET cutoff = EXCLUDED.cutoff;
        """;
        jdbcTemplate.update(setCutoffTime, raceId.value, cutoffTime.toString());
    }

    public void setCutoffYear(Instant cutoffTime, Year year) {
        final String setCutoffTime = """
            INSERT INTO year_cutoffs (year, cutoff) VALUES (?, ?)
            ON CONFLICT (year)
            DO UPDATE SET cutoff = EXCLUDED.cutoff;
        """;
        jdbcTemplate.update(setCutoffTime, year.value, cutoffTime.toString());
    }

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

    public void insertFlagStats(Flag flag, int round, RaceId raceId, SessionType sessionType) {
        final String sql = "INSERT INTO flag_stats (flag_name, race_id, round, session_type) VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(sql, flag.value, raceId.value, round, sessionType.value);
    }

    public void deleteFlagStatsById(int flagId) {
        final String sql = "DELETE FROM flag_stats WHERE flag_id = ?;";
        jdbcTemplate.update(sql, flagId);
    }

    public List<Flag> getFlags() {
        final String sql = "SELECT flag_name FROM flags;";
        return jdbcTemplate.queryForList(sql, String.class).stream()
                .map(Flag::new)
                .toList();
    }

    public boolean isValidDriverYear(Driver driver, Year year) {
        final String existCheck = "SELECT COUNT(*) FROM drivers_year WHERE year = ? AND driver_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, year.value, driver.value) > 0;
    }

    public boolean isValidDriver(Driver driver) {
        final String existCheck = "SELECT COUNT(*) FROM drivers WHERE driver_name = ?;";
        return jdbcTemplate.queryForObject(existCheck, Integer.class, driver.value) > 0;
    }

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
                                userRespository.findById((UUID) row.get("id")).get()
                                , (String) row.get("email"))
                )
                .toList();
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

    public List<UserEntity> getBingomasters() {
        final String getAllUsersSql = """
                SELECT u.user_id AS id, u.username AS username, u.google_id AS google_id
                FROM users u
                JOIN bingomasters bm ON u.user_id = bm.user_id
                ORDER BY u.username;
                """;
        return jdbcTemplate.queryForList(getAllUsersSql).stream()
                .map(row -> userRespository.findById((UUID) row.get("id")))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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
        try {
            Year year = raceService.getYearFromRaceId(raceId);
            return getAlternativeDriverName(driver, year);
        } catch (InvalidRaceException ignored) {
            return driver;
        }
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

    public Summary getSummary(RaceId raceId, Year year, PublicUserDto user) {
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
                categoriesRes = jdbcTemplate.queryForList(categoriesSql, raceId.value, user.id());
                totalRes = jdbcTemplate.queryForMap(totalSql, raceId.value, user.id());
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
                categoriesRes = jdbcTemplate.queryForList(categoriesSql, year.value, user.id());
                totalRes = jdbcTemplate.queryForMap(totalSql, year.value, user.id());
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
        if (yearService.isFinishedYear(year)) {
            return;
        }
        yearService.finalizeYear(year);
        final String addPlacement = "INSERT INTO placements_year (year, user_id, placement) VALUES (?, ?, ?);";
        List<RankedGuesser> leaderboard = getLeaderboard(year);
        for (RankedGuesser rankedGuesser : leaderboard) {
            jdbcTemplate.update(addPlacement, year.value, rankedGuesser.guesser().id(), rankedGuesser.rank().value());
        }
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
}
