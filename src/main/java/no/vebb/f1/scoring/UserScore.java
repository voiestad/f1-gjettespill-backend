package no.vebb.f1.scoring;

import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.guessing.Category;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.util.collection.IFlagGuessed;
import no.vebb.f1.util.collection.IUserRaceGuessTable;
import no.vebb.f1.util.collection.userTables.FlagGuess;
import no.vebb.f1.util.collection.userTables.PlaceGuess;
import no.vebb.f1.util.collection.userTables.StandingsGuess;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.year.Year;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserScore {

    private final RaceService raceService;
    private final GuessService guessService;
    private final ScoreService scoreService;
    private final ResultService resultService;
    public final PublicUserDto user;
    public final Year year;
    public final RaceId raceId;
    public final RacePosition racePos;
    public final List<StandingsGuess<Driver>> driversGuesses = new ArrayList<>();
    public final List<StandingsGuess<Constructor>> constructorsGuesses = new ArrayList<>();
    public final List<FlagGuess> flagGuesses = new ArrayList<>();
    public final List<PlaceGuess> winnerGuesses = new ArrayList<>();
    public final List<PlaceGuess> tenthGuesses = new ArrayList<>();

    public UserScore(
            PublicUserDto user,
            Year year,
            RaceId raceId,
            RaceService raceService,
            GuessService guessService,
            ScoreService scoreService,
            ResultService resultService
    ) {
        this.user = user;
        this.year = year;
        this.raceId = raceId;
        this.raceService = raceService;
        this.racePos = getRacePosition();
        this.guessService = guessService;
        this.scoreService = scoreService;
        this.resultService = resultService;
        initializeDriversGuesses();
        initializeConstructorsGuesses();
        initializeFlagsGuesses();
        initializeWinnerGuesses();
        initializeTenthGuesses();
    }

    public UserScore(
            PublicUserDto user,
            Year year,
            RaceService raceService,
            GuessService guessService,
            ScoreService scoreService,
            ResultService resultService
    ) {
        this(user, year, UserScore.getRaceId(year, raceService), raceService, guessService, scoreService, resultService);
    }

    private static RaceId getRaceId(Year year, RaceService raceService) {
        try {
            return raceService.getLatestRaceId(year);
        } catch (InvalidYearException e) {
            return null;
        }
    }

    private RacePosition getRacePosition() {
        if (raceId == null) {
            return null;
        }
        return raceService.getPositionOfRace(raceId);
    }

    private void initializeDriversGuesses() {
        List<Driver> guessedDriver = guessService.getGuessedYearDriver(year, user.id());
        List<Driver> drivers = resultService.getDriverStandings(raceId, year);
        getGuessedToPos(Category.DRIVER, guessedDriver, drivers, driversGuesses);
    }

    private void initializeConstructorsGuesses() {
        List<Constructor> guessedConstructor = guessService.getGuessedYearConstructor(year, user.id());
        List<Constructor> constructors = resultService.getConstructorStandings(raceId, year);
        getGuessedToPos(Category.CONSTRUCTOR, guessedConstructor, constructors, constructorsGuesses);
    }

    private <T> void getGuessedToPos(Category category, List<T> guessed,
                                     List<T> competitors, List<StandingsGuess<T>> result) {
        DiffPointsMap map = new DiffPointsMap(category, year, scoreService);
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
        DiffPointsMap map = new DiffPointsMap(Category.FLAG, year, scoreService);

        List<IFlagGuessed> sqlRes = guessService.getDataForFlagTable(racePos, year, user.id());
        for (IFlagGuessed row : sqlRes) {
            Flag flag = new Flag(row.getFlagName());
            int guessed = row.getGuessed();
            int actual = row.getActual();
            Diff diff = new Diff(Math.abs(guessed - actual));
            Points points = map.getPoints(diff);
            flagGuesses.add(new FlagGuess(flag, guessed, actual, diff, points));
        }
    }

    private void initializeWinnerGuesses() {
        getDriverPlaceGuessTable(Category.FIRST, 1, winnerGuesses);
    }

    private void initializeTenthGuesses() {
        getDriverPlaceGuessTable(Category.TENTH, 10, tenthGuesses);
    }

    private void getDriverPlaceGuessTable(Category category, int targetPos, List<PlaceGuess> result) {
        if (raceId == null) {
            return;
        }
        DiffPointsMap map = new DiffPointsMap(category, year, scoreService);
        List<IUserRaceGuessTable> sqlRes = guessService.getDataForPlaceGuessTable(category, user.id(), year, racePos);

        for (IUserRaceGuessTable row : sqlRes) {
            RacePosition racePosition = row.getRacePosition();
            String raceName = row.getRaceName();
            String driver = row.getDriverName();
            int startPos = row.getStartPosition();
            int finishPos = row.getFinishingPosition();
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
