package no.vebb.f1.guessing;

import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserRespository;
import no.vebb.f1.util.collection.*;
import no.vebb.f1.util.domainPrimitive.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GuessService {

    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final UserRespository userRespository;

    public GuessService(CategoryRepository categoryRepository, JdbcTemplate jdbcTemplate, UserRespository userRespository) {
        this.categoryRepository = categoryRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.userRespository = userRespository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryEntity::categoryName)
                .map(Category::new)
                .toList();
    }

    public boolean isValidCategory(String category) {
        return categoryRepository.existsById(category);
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
}
