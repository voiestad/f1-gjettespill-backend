package no.vebb.f1.scoring;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.util.collection.userTables.FlagGuess;
import no.vebb.f1.util.collection.userTables.PlaceGuess;
import no.vebb.f1.util.collection.userTables.StandingsGuess;
import no.vebb.f1.util.domainPrimitive.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserScore {

    private final Database db;
    public final PublicUser user;
    public final Year year;
    public final RaceId raceId;
    public final int racePos;
    public final List<StandingsGuess<Driver>> driversGuesses = new ArrayList<>();
    public final List<StandingsGuess<Constructor>> constructorsGuesses = new ArrayList<>();
    public final List<FlagGuess> flagGuesses = new ArrayList<>();
    public final List<PlaceGuess> winnerGuesses = new ArrayList<>();
    public final List<PlaceGuess> tenthGuesses = new ArrayList<>();

    public UserScore(PublicUser user, Year year, RaceId raceId, Database db) {
        this.user = user;
        this.year = year;
        this.raceId = raceId;
        this.db = db;
        this.racePos = getRacePosition();
        initializeDriversGuesses();
        initializeConstructorsGuesses();
        initializeFlagsGuesses();
        initializeWinnerGuesses();
        initializeTenthGuesses();
    }

    public UserScore(PublicUser user, Year year, Database db) {
        this(user, year, UserScore.getRaceId(year, db), db);
    }

    private static RaceId getRaceId(Year year, Database db) {
        try {
            return db.getLatestRaceId(year);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private int getRacePosition() {
        if (raceId == null) {
            return 0;
        }
        return db.getPositionOfRace(raceId);
    }

    private void initializeDriversGuesses() {
        List<Driver> guessedDriver = db.getGuessedYearDriver(year, user.id);
        List<Driver> drivers = db.getDriverStandings(raceId, year);
        Category category = new Category("DRIVER", db);
        getGuessedToPos(category, guessedDriver, drivers, driversGuesses);
    }

    private void initializeConstructorsGuesses() {
        List<Constructor> guessedConstructor = db.getGuessedYearConstructor(year, user.id);
        List<Constructor> constructors = db.getConstructorStandings(raceId, year);
        Category category = new Category("CONSTRUCTOR", db);
        getGuessedToPos(category, guessedConstructor, constructors, constructorsGuesses);
    }

    private <T> void getGuessedToPos(Category category, List<T> guessed,
                                     List<T> competitors, List<StandingsGuess<T>> result) {
        DiffPointsMap map = new DiffPointsMap(category, year, db);
        Map<T, Integer> guessedToPos = new HashMap<>();
        for (int i = 0; i < guessed.size(); i++) {
            guessedToPos.put(guessed.get(i), i + 1);
        }
        for (int i = 0; i < competitors.size(); i++) {
            int actualPos = i + 1;
            T competitor = competitors.get(i);
            Integer guessedPos = guessedToPos.get(competitor);
            if (guessedPos == null) {
                result.add(new StandingsGuess<>(actualPos, competitor, null, null, new Points()));
            } else {
                Diff diff = new Diff(Math.abs(actualPos - guessedPos));
                Points points = map.getPoints(diff);
                result.add(new StandingsGuess<>(actualPos, competitor, guessedPos, diff, points));
            }
        }
    }

    private void initializeFlagsGuesses() {
        Category category = new Category("FLAG", db);
        DiffPointsMap map = new DiffPointsMap(category, year, db);

        List<Map<String, Object>> sqlRes = db.getDataForFlagTable(racePos, year, user.id);
        for (Map<String, Object> row : sqlRes) {
            Flag flag = new Flag((String) row.get("type"), db);
            int guessed = (int) row.get("guessed");
            int actual = (int) row.get("actual");
            Diff diff = new Diff(Math.abs(guessed - actual));
            Points points = map.getPoints(diff);
            flagGuesses.add(new FlagGuess(flag, guessed, actual, diff, points));
        }
    }

    private void initializeWinnerGuesses() {
        getDriverPlaceGuessTable(new Category("FIRST", db), 1, winnerGuesses);
    }

    private void initializeTenthGuesses() {
        getDriverPlaceGuessTable(new Category("TENTH", db), 10, tenthGuesses);
    }

    private void getDriverPlaceGuessTable(Category category, int targetPos, List<PlaceGuess> result) {
        if (raceId == null) {
            return;
        }
        DiffPointsMap map = new DiffPointsMap(category, year, db);
        List<Map<String, Object>> sqlRes = db.getDataForPlaceGuessTable(category, user.id, year, racePos);

        for (Map<String, Object> row : sqlRes) {
            int racePosition = (int) row.get("race_position");
            String raceName = (String) row.get("race_name");
            String driver = (String) row.get("driver");
            int startPos = (int) row.get("start");
            int finishPos = (int) row.get("finish");
            Diff diff = new Diff(Math.abs(targetPos - finishPos));
            Points points = map.getPoints(diff);
            result.add(new PlaceGuess(racePosition, raceName, driver, startPos, finishPos, diff, points));
        }
    }

    public Points getScore() {
        Points score = new Points();
        score = driversGuesses.stream().map(StandingsGuess::points).reduce(score, Points::add);
        score = constructorsGuesses.stream().map(StandingsGuess::points).reduce(score, Points::add);
        score = flagGuesses.stream().map(FlagGuess::points).reduce(score, Points::add);
        score = winnerGuesses.stream().map(PlaceGuess::points).reduce(score, Points::add);
        score = tenthGuesses.stream().map(PlaceGuess::points).reduce(score, Points::add);
        return score;
    }
}
