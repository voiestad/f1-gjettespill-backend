package no.vebb.f1.scoring;

import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.domain.ConstructorName;
import no.vebb.f1.competitors.domain.DriverName;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.results.domain.CompetitorPosition;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.scoring.diffPointsMap.DiffPointsMap;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.guessing.collection.IFlagGuessed;
import no.vebb.f1.guessing.collection.IUserRaceGuessTable;
import no.vebb.f1.scoring.userTables.FlagGuess;
import no.vebb.f1.scoring.userTables.PlaceGuess;
import no.vebb.f1.scoring.userTables.StandingsGuess;
import no.vebb.f1.year.Year;

import java.util.*;

public class UserScore {

    private final RaceService raceService;
    private final GuessService guessService;
    private final ScoreService scoreService;
    private final ResultService resultService;
    public final PublicUserDto user;
    public final Year year;
    public final RaceId raceId;
    public final RacePosition racePos;
    public final List<StandingsGuess<DriverName>> driversGuesses = new ArrayList<>();
    public final List<StandingsGuess<ConstructorName>> constructorsGuesses = new ArrayList<>();
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
        return raceService.getLatestRaceId(year).orElse(null);
    }

    private RacePosition getRacePosition() {
        return raceId == null ? null : raceService.getPositionOfRace(raceId).orElse(null);
    }

    private void initializeDriversGuesses() {
        List<DriverName> guessedDriver = guessService.getGuessedYearDriver(year, user.id()).stream().map(DriverEntity::driverName).toList();
        List<DriverName> drivers = resultService.getDriverStandings(raceId, year).stream().map(DriverEntity::driverName).toList();
        getGuessedToPos(Category.DRIVER, guessedDriver, drivers, driversGuesses);
    }

    private void initializeConstructorsGuesses() {
        List<ConstructorName> guessedConstructor = guessService.getGuessedYearConstructor(year, user.id()).stream().map(ConstructorEntity::constructorName).toList();
        List<ConstructorName> constructors = resultService.getConstructorStandings(raceId, year).stream().map(ConstructorEntity::constructorName).toList();
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
                result.add(new StandingsGuess<>(actualPos, competitor, null, null, new UserPoints()));
            } else {
                Diff diff = new Diff(actualPos - guessedPos);
                UserPoints points = map.getPoints(diff);
                result.add(new StandingsGuess<>(actualPos, competitor, guessedPos, diff, points));
            }
        }
    }

    private void initializeFlagsGuesses() {
        DiffPointsMap map = new DiffPointsMap(Category.FLAG, year, scoreService);

        List<IFlagGuessed> sqlRes = guessService.getDataForFlagTable(racePos, year, user.id());
        for (IFlagGuessed row : sqlRes) {
            Flag flag = row.getFlagName();
            int guessed = row.getGuessed();
            int actual = row.getActual();
            Diff diff = new Diff(guessed - actual);
            UserPoints points = map.getPoints(diff);
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
            DriverName driver = row.getDriverName();
            CompetitorPosition startPos = row.getStartPosition();
            CompetitorPosition finishPos = row.getFinishingPosition();
            Diff diff = new Diff(targetPos - finishPos.toValue());
            UserPoints points = map.getPoints(diff);
            result.add(new PlaceGuess(racePosition, raceName, driver, startPos, finishPos, diff, points));
        }
    }

    public UserPoints getScore() {
        UserPoints score = new UserPoints();
        score = score.add(getDriversScore());
        score = score.add(getConstructorsScore());
        score = score.add(getFlagScore());
        score = score.add(getWinnerScore());
        score = score.add(getTenthScore());
        return score;
    }

    public UserPoints getDriversScore() {
        return driversGuesses.stream().map(StandingsGuess::points).reduce(new UserPoints(), UserPoints::add);
    }

    public UserPoints getConstructorsScore() {
        return constructorsGuesses.stream().map(StandingsGuess::points).reduce(new UserPoints(), UserPoints::add);
    }

    public UserPoints getFlagScore() {
        return flagGuesses.stream().map(FlagGuess::points).reduce(new UserPoints(), UserPoints::add);
    }

    public UserPoints getWinnerScore() {
        return winnerGuesses.stream().map(PlaceGuess::points).reduce(new UserPoints(), UserPoints::add);
    }

    public UserPoints getTenthScore() {
        return tenthGuesses.stream().map(PlaceGuess::points).reduce(new UserPoints(), UserPoints::add);
    }
}
