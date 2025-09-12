package no.vebb.f1.controller;

import java.util.*;
import java.util.function.BiFunction;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.guessing.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.guessing.constructor.ConstructorGuessEntity;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.guessing.driver.DriverGuessEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.year.YearService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.user.UserService;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.collection.Flags;
import no.vebb.f1.collection.Race;
import no.vebb.f1.collection.ColoredCompetitor;
import no.vebb.f1.collection.CutoffCompetitors;
import no.vebb.f1.collection.CutoffCompetitorsSelected;
import no.vebb.f1.collection.CutoffFlags;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

@RestController
@RequestMapping("/api/guess")
public class GuessController {

    private static final Logger logger = LoggerFactory.getLogger(GuessController.class);
    private final UserService userService;
    private final CutoffService cutoffService;
    private final ResultService resultService;
    private final YearService yearService;
    private final RaceService raceService;
    private final CompetitorService competitorService;
    private final GuessService guessService;

    public GuessController(UserService userService, CutoffService cutoffService, ResultService resultService, YearService yearService, RaceService raceService, CompetitorService competitorService, GuessService guessService) {
        this.userService = userService;
        this.cutoffService = cutoffService;
        this.resultService = resultService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.competitorService = competitorService;
        this.guessService = guessService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> guess() {
        List<Category> res = new ArrayList<>();
        if (cutoffService.getCurrentYearIfAbleToGuess().isPresent()) {
            res.add(Category.DRIVER);
            res.add(Category.CONSTRUCTOR);
            res.add(Category.FLAG);
        }
        if (getRaceIdToGuess().isPresent()) {
            res.add(Category.FIRST);
            res.add(Category.TENTH);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private <T extends Competitor> ResponseEntity<CutoffCompetitors<T>> getRankCompetitors(BiFunction<UUID, Year, List<ColoredCompetitor<T>>> getCompetitors) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        UUID id = userService.getUser().id();
        long timeLeftToGuess = cutoffService.getTimeLeftToGuessYear(year);
        List<ColoredCompetitor<T>> competitors = getCompetitors.apply(id, year);
        CutoffCompetitors<T> res = new CutoffCompetitors<>(competitors, timeLeftToGuess);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/driver")
    public ResponseEntity<CutoffCompetitors<Driver>> rankDrivers() {
        return getRankCompetitors(guessService::getDriversGuess);
    }

    @PostMapping("/driver")
    @Transactional
    public ResponseEntity<?> rankDrivers(@RequestParam List<String> rankedCompetitors) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        UUID id = userService.getUser().id();
        List<Driver> validationList = competitorService.getDriversYear(year);
        Set<Driver> competitors = new HashSet<>(validationList);
        List<Driver> guessedDrivers = rankedCompetitors.stream()
                .map(competitorService::getDriver)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        String error = validateGuessList(guessedDrivers, competitors);
        if (error != null) {
            logger.warn(error);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GuessPosition position = new GuessPosition();
        List<DriverGuessEntity> driverGuesses = new ArrayList<>();
        for (Driver driver : guessedDrivers) {
            driverGuesses.add(new DriverGuessEntity(id, position, year, driver));
            position = position.next();
        }
        guessService.addDriversYearGuesses(driverGuesses);
        logger.info("User guessed on driver");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/constructor")
    public ResponseEntity<CutoffCompetitors<Constructor>> rankConstructors() {
        return getRankCompetitors(guessService::getConstructorsGuess);
    }
    @PostMapping("/constructor")
    @Transactional
    public ResponseEntity<?> rankConstructors(@RequestParam List<String> rankedCompetitors) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        List<Constructor> validationList = competitorService.getConstructorsYear(year);
        Set<Constructor> competitors = new HashSet<>(validationList);
        List<Constructor> guessedConstructors = rankedCompetitors.stream()
                .map(competitorService::getConstructor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        String error = validateGuessList(guessedConstructors, competitors);
        if (error != null) {
            logger.warn(error);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GuessPosition position = new GuessPosition();
        UUID id = userService.getUser().id();
        List<ConstructorGuessEntity> constructorGuesses = new ArrayList<>();
        for (Constructor constructor : guessedConstructors) {
            constructorGuesses.add(new ConstructorGuessEntity(id, position, year, constructor));
            position = position.next();
        }
        guessService.addConstructorsYearGuesses(constructorGuesses);
        logger.info("User guessed on constructor");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private <T extends Competitor> String validateGuessList(List<T> guessed, Set<T> original) {
        Set<T> guessedSet = new HashSet<>();
        for (T competitor : guessed) {
            if (!original.contains(competitor)) {
                return String.format("%s is not a valid competitor.", competitor);
            }
            if (!guessedSet.add(competitor)) {
                return String.format("Competitor %s is guessed twice in the ranking.", competitor);
            }
        }
        if (original.size() != guessedSet.size()) {
            return String.format("Not all competitors are guessed. Expected %d, was %d.", original.size(),
                    guessedSet.size());
        }
        return null;
    }

    @GetMapping("/tenth")
    public ResponseEntity<CutoffCompetitorsSelected<Driver>> guessTenth() {
        return handleGetChooseDriver(Category.TENTH);
    }

    @PostMapping("/tenth")
    @Transactional
    public ResponseEntity<?> guessTenth(@RequestParam Driver driver) {
        return handlePostChooseDriver(driver, Category.TENTH);
    }

    @GetMapping("/first")
    public ResponseEntity<CutoffCompetitorsSelected<Driver>> guessWinner() {
        return handleGetChooseDriver(Category.FIRST);
    }

    @PostMapping("/first")
    @Transactional
    public ResponseEntity<?> guessWinner(@RequestParam Driver driver) {
        return handlePostChooseDriver(driver, Category.FIRST);
    }

    private ResponseEntity<CutoffCompetitorsSelected<Driver>> handleGetChooseDriver(Category category) {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Year year = optYear.get();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<RaceId> optRaceId = getRaceIdToGuess();
        if (optRaceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        RaceId raceId = optRaceId.get();
        Optional<Race> optRace = raceService.getRaceFromId(raceId);
        if (optRace.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Race race = optRace.get();
        long timeLeftToGuess = cutoffService.getTimeLeftToGuessRace(raceId);
        List<ColoredCompetitor<Driver>> drivers = resultService.getDriversFromStartingGridWithColors(raceId);
        UUID id = userService.getUser().id();
        Driver driver = guessService.getGuessedDriverPlace(raceId, category, id);
        CutoffCompetitorsSelected<Driver> res = new CutoffCompetitorsSelected<>(drivers, driver, timeLeftToGuess, race);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private ResponseEntity<?> handlePostChooseDriver(Driver driver, Category category) {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Year year = optYear.get();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and not available for guessing",
                    HttpStatus.FORBIDDEN);
        }
        Optional<RaceId> optRaceId = getRaceIdToGuess();
        if (optRaceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        RaceId raceId = optRaceId.get();
        Set<Driver> driversCheck = new HashSet<>(resultService.getDriversFromStartingGrid(raceId));
        if (!driversCheck.contains(driver)) {
            logger.warn("'{}', invalid winner driver inputted by user.", driver);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UUID id = userService.getUser().id();
        guessService.addDriverPlaceGuess(id, raceId, driver, category);
        logger.info("User guessed on category '{}' on race '{}'", category, raceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Optional<RaceId> getRaceIdToGuess() {
        return resultService.getCurrentRaceIdToGuess().filter(cutoffService::isAbleToGuessRace);
    }

    @GetMapping("/flag")
    public ResponseEntity<CutoffFlags> guessFlags() {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        long timeLeftToGuess = cutoffService.getTimeLeftToGuessYear(year);
        Flags flags = guessService.getFlagGuesses(userService.getUser().id(), year);
        CutoffFlags res = new CutoffFlags(flags, timeLeftToGuess);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/flag")
    @Transactional
    public ResponseEntity<?> guessFlags(@RequestParam int yellow, @RequestParam int red,
                                        @RequestParam int safetyCar) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        Flags flags = new Flags(yellow, red, safetyCar);
        if (!flags.hasValidValues()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UUID id = userService.getUser().id();
        guessService.addFlagGuesses(id, year, flags);
        logger.info("User guessed on flags on year");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
