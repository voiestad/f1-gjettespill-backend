package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.util.exception.*;
import no.vebb.f1.year.YearService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
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
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Flags;
import no.vebb.f1.util.collection.Race;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.collection.CutoffCompetitors;
import no.vebb.f1.util.collection.CutoffCompetitorsSelected;
import no.vebb.f1.util.collection.CutoffFlags;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;

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
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			if (yearService.isFinishedYear(year)) {
				throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
			}
		} catch (InvalidYearException ignored) {
		}
		if (cutoffService.isAbleToGuessCurrentYear()) {
			res.add(new Category("DRIVER"));
			res.add(new Category("CONSTRUCTOR"));
			res.add(new Category("FLAG"));
		}
		if (isRaceToGuess()) {
			res.add(new Category("FIRST"));
			res.add(new Category("TENTH"));
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	/**
	 * Handles GET requests for /guess/drivers. If it is still possible to guess,
	 * gives a list of drivers to rank and the time left to guess.
	 */
	@GetMapping("/driver")
	public ResponseEntity<CutoffCompetitors<Driver>> rankDrivers() {
		if (!cutoffService.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		if (yearService.isFinishedYear(year)) {
			throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
		}
		UUID id = userService.getUser().id();
		long timeLeftToGuess = cutoffService.getTimeLeftToGuessYear();
		List<ColoredCompetitor<Driver>> competitors = guessService.getDriversGuess(id, year);
		CutoffCompetitors<Driver> res = new CutoffCompetitors<>(competitors, timeLeftToGuess);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PostMapping("/driver")
	@Transactional
	public ResponseEntity<?> rankDrivers(@RequestParam List<String> rankedCompetitors) {
		if (!cutoffService.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		if (yearService.isFinishedYear(year)) {
			throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
		}
		try {
			List<Driver> validationList = competitorService.getDriversYear(year);
			Set<Driver> competitors = new HashSet<>(validationList);
			List<Driver> guessedDrivers = rankedCompetitors.stream()
					.map(driver -> new Driver(driver, competitorService))
					.toList();
			String error = validateGuessList(guessedDrivers, competitors);
			if (error != null) {
				logger.warn(error);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			int position = 1;
			UUID id = userService.getUser().id();
			for (Driver driver : guessedDrivers) {
				guessService.insertDriversYearGuess(id, driver, year, position);
				position++;
			}
			logger.info("User '{}' guessed on '{}' on year '{}'", id, "driver", year);
		return new ResponseEntity<>(HttpStatus.OK);
		} catch (InvalidDriverException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/constructor")
	public ResponseEntity<CutoffCompetitors<Constructor>> rankConstructors() {
		if (!cutoffService.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		if (yearService.isFinishedYear(year)) {
			throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
		}
		UUID id = userService.getUser().id();
		long timeLeftToGuess = cutoffService.getTimeLeftToGuessYear();
		List<ColoredCompetitor<Constructor>> competitors = guessService.getConstructorsGuess(id, year);
		CutoffCompetitors<Constructor> res = new CutoffCompetitors<>(competitors, timeLeftToGuess);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PostMapping("/constructor")
	@Transactional
	public ResponseEntity<?> rankConstructors(@RequestParam List<String> rankedCompetitors) {
		if (!cutoffService.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		if (yearService.isFinishedYear(year)) {
			throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
		}
		try {
			List<Constructor> validationList = competitorService.getConstructorsYear(year);
			Set<Constructor> competitors = new HashSet<>(validationList);
			List<Constructor> guessedConstructors = rankedCompetitors.stream()
					.map(constructor -> new Constructor(constructor, competitorService))
					.toList();
			String error = validateGuessList(guessedConstructors, competitors);
			if (error != null) {
				logger.warn(error);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			int position = 1;
			UUID id = userService.getUser().id();
			for (Constructor constructor : guessedConstructors) {
				guessService.insertConstructorsYearGuess(id, constructor, year, position);
				position++;
			}
			logger.info("User '{}' guessed on '{}' on year '{}'", id, "constructor", year);
		} catch (InvalidConstructorException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private <T> String validateGuessList(List<T> guessed, Set<T> original) {
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
		Category category = new Category("TENTH", guessService);
		return handleGetChooseDriver(category);
	}

	@PostMapping("/tenth")
	@Transactional
	public ResponseEntity<?> guessTenth(@RequestParam String driver) {
		Category category = new Category("TENTH", guessService);
		return handlePostChooseDriver(driver, category);
	}

	@GetMapping("/first")
	public ResponseEntity<CutoffCompetitorsSelected<Driver>> guessWinner() {
		Category category = new Category("FIRST", guessService);
		return handleGetChooseDriver(category);
	}

	@PostMapping("/first")
	@Transactional
	public ResponseEntity<?> guessWinner(@RequestParam String driver) {
		Category category = new Category("FIRST", guessService);
		return handlePostChooseDriver(driver, category);
	}

	private ResponseEntity<CutoffCompetitorsSelected<Driver>> handleGetChooseDriver(Category category) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			if (yearService.isFinishedYear(year)) {
				throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
			}
			RaceOrderEntity raceOrderEntity = raceService.getRaceFromId(getRaceIdToGuess());
			RaceId raceId = new RaceId(raceOrderEntity.raceId());
			Race race = new Race(raceOrderEntity.position(), raceOrderEntity.name(), raceId, year);
			long timeLeftToGuess = cutoffService.getTimeLeftToGuessRace(raceId);
			List<ColoredCompetitor<Driver>> drivers = resultService.getDriversFromStartingGridWithColors(raceId);
			UUID id = userService.getUser().id();
			try {
				Driver driver = guessService.getGuessedDriverPlace(raceId, category, id);
				CutoffCompetitorsSelected<Driver> res = new CutoffCompetitorsSelected<>(drivers, driver, timeLeftToGuess, race);
				return new ResponseEntity<>(res, HttpStatus.OK);
			} catch (EmptyResultDataAccessException e) {
				CutoffCompetitorsSelected<Driver> res = new CutoffCompetitorsSelected<>(drivers, null, timeLeftToGuess, race);
				return new ResponseEntity<>(res, HttpStatus.OK);
			}
		} catch (NoAvailableRaceException | InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private ResponseEntity<?> handlePostChooseDriver(String driver, Category category) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			if (yearService.isFinishedYear(year)) {
				throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
			}
			RaceId raceId = getRaceIdToGuess();
			Driver validDriver = new Driver(driver, competitorService);
			Set<Driver> driversCheck = new HashSet<>(resultService.getDriversFromStartingGrid(raceId));
			if (!driversCheck.contains(validDriver)) {
				logger.warn("'{}', invalid winner driver inputted by user.", driver);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			UUID id = userService.getUser().id();
			guessService.addDriverPlaceGuess(id, raceId, validDriver, category);
			logger.info("User '{}' guessed on category '{}' on race '{}'", id, category, raceId);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (NoAvailableRaceException | InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (InvalidDriverException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	private RaceId getRaceIdToGuess() throws NoAvailableRaceException {
        RaceId raceId = resultService.getCurrentRaceIdToGuess();
        if (!cutoffService.isAbleToGuessRace(raceId)) {
            throw new NoAvailableRaceException("Cutoff has been passed");
        }
        return raceId;
	}

	@GetMapping("/flag")
	public ResponseEntity<CutoffFlags> guessFlags() {
		if (!cutoffService.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		long timeLeftToGuess = cutoffService.getTimeLeftToGuessYear();
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		if (yearService.isFinishedYear(year)) {
			throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
		}
		Flags flags = guessService.getFlagGuesses(userService.getUser().id(), year);
		CutoffFlags res = new CutoffFlags(flags, timeLeftToGuess);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@PostMapping("/flag")
	@Transactional
	public ResponseEntity<?> guessFlags(@RequestParam int yellow, @RequestParam int red,
			@RequestParam int safetyCar) {
		if (!cutoffService.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		if (yearService.isFinishedYear(year)) {
			throw new YearFinishedException("Year '" + year + "' is over and not available for guessing");
		}
		Flags flags = new Flags(yellow, red, safetyCar);
		if (!flags.hasValidValues()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		UUID id = userService.getUser().id();
		guessService.addFlagGuesses(id, year, flags);
		logger.info("User '{}' guessed on flags on year '{}'", id, year);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private boolean isRaceToGuess() {
		try {
			getRaceIdToGuess();
			return true;
		} catch (NoAvailableRaceException e) {
			return false;
		}
	}

}
