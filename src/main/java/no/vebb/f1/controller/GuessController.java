package no.vebb.f1.controller;

import java.util.*;
import java.util.function.BiFunction;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.domain.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.competitors.driver.DriverId;
import no.vebb.f1.guessing.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.guessing.constructor.ConstructorGuessEntity;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.guessing.driver.DriverGuessEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.results.startingGrid.StartingGridEntity;
import no.vebb.f1.year.YearService;
import no.vebb.f1.user.UserService;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.collection.Flags;
import no.vebb.f1.collection.Race;
import no.vebb.f1.collection.CutoffCompetitors;
import no.vebb.f1.collection.CutoffCompetitorsSelected;
import no.vebb.f1.collection.CutoffFlags;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

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

    public GuessController(
            UserService userService,
            CutoffService cutoffService,
            ResultService resultService,
            YearService yearService,
            RaceService raceService,
            CompetitorService competitorService,
            GuessService guessService) {
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

    private ResponseEntity<CutoffCompetitors> getRankCompetitors(BiFunction<UUID, Year, List<CompetitorDTO>> getCompetitors) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        UUID id = userService.getUser().id();
        long timeLeftToGuess = cutoffService.getTimeLeftToGuessYear(year);
        List<CompetitorDTO> competitors = getCompetitors.apply(id, year);
        CutoffCompetitors res = new CutoffCompetitors(competitors, timeLeftToGuess);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/driver")
    public ResponseEntity<CutoffCompetitors> rankDrivers() {
        return getRankCompetitors(guessService::getDriversGuess);
    }

    @PostMapping("/driver")
    @Transactional
    public ResponseEntity<?> rankDrivers(@RequestParam List<Integer> rankedCompetitors) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        UUID userId = userService.getUser().id();
        Set<DriverEntity> validationSet = new HashSet<>(competitorService.getDriversYear(year));
        List<DriverEntity> guessedDrivers = competitorService.getAllDrivers(rankedCompetitors);
        String error = validateGuessList(guessedDrivers, validationSet);
        if (error != null) {
            logger.warn(error);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GuessPosition position = new GuessPosition();
        List<DriverGuessEntity> driverGuesses = new ArrayList<>();
        for (DriverEntity driverEntity : guessedDrivers) {
            driverGuesses.add(new DriverGuessEntity(userId, position, year, driverEntity));
            position = position.next();
        }
        guessService.addDriversYearGuesses(driverGuesses);
        logger.info("User guessed on driver");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/constructor")
    public ResponseEntity<CutoffCompetitors> rankConstructors() {
        return getRankCompetitors(guessService::getConstructorsGuess);
    }

    @PostMapping("/constructor")
    @Transactional
    public ResponseEntity<?> rankConstructors(@RequestParam List<Integer> rankedCompetitors) {
        Optional<Year> optYear = cutoffService.getCurrentYearIfAbleToGuess();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Year year = optYear.get();
        Set<ConstructorEntity> validationSet = new HashSet<>(competitorService.getConstructorsYear(year));
        List<ConstructorEntity> guessedConstructors = competitorService.getAllConstructors(rankedCompetitors);
        String error = validateGuessList(guessedConstructors, validationSet);
        if (error != null) {
            logger.warn(error);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        GuessPosition position = new GuessPosition();
        UUID id = userService.getUser().id();
        List<ConstructorGuessEntity> constructorGuesses = new ArrayList<>();
        for (ConstructorEntity constructorEntity : guessedConstructors) {
            constructorGuesses.add(new ConstructorGuessEntity(id, position, year, constructorEntity));
            position = position.next();
        }
        guessService.addConstructorsYearGuesses(constructorGuesses);
        logger.info("User guessed on constructor");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private <T> String validateGuessList(List<T> guessed, Set<T> validationSet) {
        Set<T> guessedSet = new HashSet<>();
        for (T competitor : guessed) {
            if (!validationSet.contains(competitor)) {
                return String.format("%s is not a valid competitor.", competitor);
            }
            if (!guessedSet.add(competitor)) {
                return String.format("Competitor %s is guessed twice in the ranking.", competitor);
            }
        }
        if (validationSet.size() != guessedSet.size()) {
            return String.format("Not all competitors are guessed. Expected %d, was %d.", validationSet.size(),
                    guessedSet.size());
        }
        return null;
    }

    @GetMapping("/tenth")
    public ResponseEntity<CutoffCompetitorsSelected> guessTenth() {
        return handleGetChooseDriver(Category.TENTH);
    }

    @PostMapping("/tenth")
    @Transactional
    public ResponseEntity<?> guessTenth(@RequestParam DriverEntity driver) {
        return handlePostChooseDriver(driver, Category.TENTH);
    }

    @GetMapping("/first")
    public ResponseEntity<CutoffCompetitorsSelected> guessWinner() {
        return handleGetChooseDriver(Category.FIRST);
    }

    @PostMapping("/first")
    @Transactional
    public ResponseEntity<?> guessWinner(@RequestParam DriverEntity driver) {
        return handlePostChooseDriver(driver, Category.FIRST);
    }

    private ResponseEntity<CutoffCompetitorsSelected> handleGetChooseDriver(Category category) {
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
            logger.error("Could not find Race from RaceId: {}", optRaceId.get());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Race race = optRace.get();
        long timeLeftToGuess = cutoffService.getTimeLeftToGuessRace(raceId);
        List<CompetitorDTO> drivers = resultService.getStartingGrid(raceId).stream()
                .map(StartingGridEntity::driver)
                .map(CompetitorDTO::fromEntity)
                .toList();
        UUID id = userService.getUser().id();
        CompetitorId driver = guessService.getGuessedDriverPlace(raceId, category, id);
        CutoffCompetitorsSelected res = new CutoffCompetitorsSelected(drivers, driver, timeLeftToGuess, race);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private ResponseEntity<?> handlePostChooseDriver(DriverEntity driver, Category category) {
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
        Set<DriverId> driversCheck = new HashSet<>(resultService.getDriversFromStartingGrid(raceId));
        if (!driversCheck.contains(driver.driverId())) {
            logger.warn("'{}', invalid winner driver inputted by user.", driver.driverName());
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
        Optional<Flags> flags = guessService.getFlagGuesses(userService.getUser().id(), year);
        if (flags.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        CutoffFlags res = new CutoffFlags(flags.get(), timeLeftToGuess);
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
        Optional<Flags> optFlags = Flags.getFlags(yellow, red, safetyCar);
        if (optFlags.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UUID id = userService.getUser().id();
        guessService.addFlagGuesses(id, year, optFlags.get());
        logger.info("User guessed on flags on year");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
