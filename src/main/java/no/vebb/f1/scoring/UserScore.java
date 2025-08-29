package no.vebb.f1.scoring;

import no.vebb.f1.database.Database;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.util.collection.userTables.FlagGuess;
import no.vebb.f1.util.collection.userTables.PlaceGuess;
import no.vebb.f1.util.collection.userTables.StandingsGuess;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.InvalidYearException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserScore {

    private final Database db;
    private final RaceService raceService;
    public final PublicUserDto user;
    public final Year year;
    public final RaceId raceId;
    public final int racePos;
    public final List<StandingsGuess<Driver>> driversGuesses = new ArrayList<>();
    public final List<StandingsGuess<Constructor>> constructorsGuesses = new ArrayList<>();
    public final List<FlagGuess> flagGuesses = new ArrayList<>();
    public final List<PlaceGuess> winnerGuesses = new ArrayList<>();
    public final List<PlaceGuess> tenthGuesses = new ArrayList<>();

    public UserScore(PublicUserDto user, Year year, RaceId raceId, Database db, RaceService raceService) {
        this.user = user;
        this.year = year;
        this.raceId = raceId;
        this.db = db;
        this.raceService = raceService;
        this.racePos = getRacePosition();
        initializeDriversGuesses();
        initializeConstructorsGuesses();
        initializeFlagsGuesses();
        initializeWinnerGuesses();
        initializeTenthGuesses();
    }

    public UserScore(PublicUserDto user, Year year, Database db, RaceService raceService) {
        this(user, year, UserScore.getRaceId(year, raceService), db, raceService);
    }

    private static RaceId getRaceId(Year year, RaceService raceService) {
        try {
            return raceService.getLatestRaceId(year);
        } catch (InvalidYearException e) {
            return null;
        }
    }

    private int getRacePosition() {
        if (raceId == null) {
            return 0;
        }
        return raceService.getPositionOfRace(raceId);
    }

    private void initializeDriversGuesses() {
        List<Driver> guessedDriver = db.getGuessedYearDriver(year, user.id());
        List<Driver> drivers = db.getDriverStandings(raceId, year);
        Category category = new Category("DRIVER", db);
        getGuessedToPos(category, guessedDriver, drivers, driversGuesses);
    }

    private void initializeConstructorsGuesses() {
        List<Constructor> guessedConstructor = db.getGuessedYearConstructor(year, user.id());
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

        List<Map<String, Object>> sqlRes = db.getDataForFlagTable(racePos, year, user.id());
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
        List<Map<String, Object>> sqlRes = db.getDataForPlaceGuessTable(category, user.id(), year, racePos);

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
        score = score.add(getDriversScore());
        score = score.add(getConstructorsScore());
        score = score.add(getFlagScore());
        score = score.add(getWinnerScore());
        score = score.add(getTenthScore());
        return score;
    }

    public Points getDriversScore() {
        return driversGuesses.stream().map(StandingsGuess::points).reduce(new Points(), Points::add);
    }

    public Points getConstructorsScore() {
        return constructorsGuesses.stream().map(StandingsGuess::points).reduce(new Points(), Points::add);
    }

    public Points getFlagScore() {
        return flagGuesses.stream().map(FlagGuess::points).reduce(new Points(), Points::add);
    }

    public Points getWinnerScore() {
        return winnerGuesses.stream().map(PlaceGuess::points).reduce(new Points(), Points::add);
    }

    public Points getTenthScore() {
        return tenthGuesses.stream().map(PlaceGuess::points).reduce(new Points(), Points::add);
    }
}
