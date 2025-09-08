package no.vebb.f1.guessing;

import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserRespository;
import no.vebb.f1.util.collection.*;
import no.vebb.f1.util.domainPrimitive.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GuessService {

    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final UserRespository userRespository;
    private final ConstructorGuessRepository constructorGuessRepository;
    private final DriverGuessRepository driverGuessRepository;
    private final FlagGuessRepository flagGuessRepository;
    private final DriverPlaceGuessRepository driverPlaceGuessRepository;

    public GuessService(CategoryRepository categoryRepository, JdbcTemplate jdbcTemplate, UserRespository userRespository, ConstructorGuessRepository constructorGuessRepository, DriverGuessRepository driverGuessRepository, FlagGuessRepository flagGuessRepository, DriverPlaceGuessRepository driverPlaceGuessRepository) {
        this.categoryRepository = categoryRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.userRespository = userRespository;
        this.constructorGuessRepository = constructorGuessRepository;
        this.driverGuessRepository = driverGuessRepository;
        this.flagGuessRepository = flagGuessRepository;
        this.driverPlaceGuessRepository = driverPlaceGuessRepository;
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
        return driverGuessRepository.findAllByIdYearAndIdUserIdOrderByIdPosition(year.value, userId).stream()
                .map(DriverGuessEntity::driverName)
                .map(Driver::new)
                .toList();
    }

    public List<Constructor> getGuessedYearConstructor(Year year, UUID userId) {
        return constructorGuessRepository.findAllByIdYearAndIdUserIdOrderByIdPosition(year.value, userId).stream()
                .map(ConstructorGuessEntity::constructorName)
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
        flagGuessRepository.saveAll(Arrays.asList(
                new FlagGuessEntity(userId, "Yellow Flag", year.value, flags.yellow),
                new FlagGuessEntity(userId, "Red Flag", year.value, flags.red),
                new FlagGuessEntity(userId, "Safety Car", year.value, flags.safetyCar)
        ));
    }

    public Flags getFlagGuesses(UUID userId, Year year) {
        Flags flags = new Flags();
        List<FlagGuessEntity> flagGuesses = flagGuessRepository.findAllByIdUserIdAndIdYear(userId, year.value);
        for (FlagGuessEntity flagGuess : flagGuesses) {
            switch (flagGuess.flagName()) {
                case "Yellow Flag":
                    flags.yellow = flagGuess.amount();
                    break;
                case "Red Flag":
                    flags.red = flagGuess.amount();
                    break;
                case "Safety Car":
                    flags.safetyCar = flagGuess.amount();
                    break;
            }
        }
        return flags;
    }
    public Driver getGuessedDriverPlace(RaceId raceId, Category category, UUID userId) {
        return driverPlaceGuessRepository.findById(new DriverPlaceGuessId(userId, raceId.value, category.value))
                .map(DriverPlaceGuessEntity::driverName)
                .map(Driver::new)
                .orElse(null);
    }

    public void addDriverPlaceGuess(UUID userId, RaceId raceId, Driver driver, Category category) {
        driverPlaceGuessRepository.save(new DriverPlaceGuessEntity(userId, raceId.value, category.value, driver.value));
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

    public void addDriversYearGuesses(List<DriverGuessEntity> driverGuesses) {
        driverGuessRepository.saveAll(driverGuesses);
    }

    public void addConstructorsYearGuesses(List<ConstructorGuessEntity> constructorGuesses) {
        constructorGuessRepository.saveAll(constructorGuesses);
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
        return driverGuessRepository.findAllByIdUserIdOrderByIdYearDescIdPosition(userId).stream()
                .map(CompetitorGuessYear::fromEntity)
                .toList();
    }

    public List<CompetitorGuessYear<Constructor>> userGuessDataConstructor(UUID userId) {
        return constructorGuessRepository.findAllByIdUserIdOrderByIdYearDescIdPosition(userId).stream()
                .map(CompetitorGuessYear::fromEntity)
                .toList();
    }

    public List<FlagGuessYear> userGuessDataFlag(UUID userId) {
        return flagGuessRepository.findAllByIdUserIdOrderByIdYearDescIdFlagName(userId).stream()
                .map(FlagGuessYear::fromEntity)
                .toList();

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
