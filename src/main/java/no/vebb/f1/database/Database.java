package no.vebb.f1.database;

import java.util.*;

import no.vebb.f1.user.UserRespository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import no.vebb.f1.util.collection.*;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserMail;

@Service
@SuppressWarnings("DataFlowIssue")
public class Database {

    private final JdbcTemplate jdbcTemplate;
    private final UserRespository userRespository;

    public Database(JdbcTemplate jdbcTemplate, UserRespository userRespository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRespository = userRespository;
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
