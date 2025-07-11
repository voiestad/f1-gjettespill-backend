package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
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
import no.vebb.f1.util.exception.InvalidConstructorException;
import no.vebb.f1.util.exception.InvalidDriverException;
import no.vebb.f1.util.exception.NoAvailableRaceException;

@RestController
@RequestMapping("/api/guess")
public class GuessController {

	private static final Logger logger = LoggerFactory.getLogger(GuessController.class);
	private final Database db;
	private final UserService userService;
	private final Cutoff cutoff;

	public GuessController(Database db, UserService userService, Cutoff cutoff) {
		this.db = db;
		this.userService = userService;
		this.cutoff = cutoff;
	}

	@GetMapping("/categories")
	public ResponseEntity<List<Category>> guess() {
		List<Category> res = new ArrayList<>();
		if (cutoff.isAbleToGuessCurrentYear()) {
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
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		UUID id = userService.getUser().id();
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		List<ColoredCompetitor<Driver>> competitors = db.getDriversGuess(id, year);
		CutoffCompetitors<Driver> res = new CutoffCompetitors<>(competitors, timeLeftToGuess);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PostMapping("/driver")
	@Transactional
	public ResponseEntity<?> rankDrivers(@RequestParam List<String> rankedCompetitors) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		try {
			List<Driver> validationList = db.getDriversYear(year);
			Set<Driver> competitors = new HashSet<>(validationList);
			List<Driver> guessedDrivers = rankedCompetitors.stream()
					.map(driver -> new Driver(driver, db))
					.toList();
			String error = validateGuessList(guessedDrivers, competitors);
			if (error != null) {
				logger.warn(error);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			int position = 1;
			UUID id = userService.getUser().id();
			for (Driver driver : guessedDrivers) {
				db.insertDriversYearGuess(id, driver, year, position);
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
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		UUID id = userService.getUser().id();
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		List<ColoredCompetitor<Constructor>> competitors = db.getConstructorsGuess(id, year);
		CutoffCompetitors<Constructor> res = new CutoffCompetitors<>(competitors, timeLeftToGuess);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PostMapping("/constructor")
	@Transactional
	public ResponseEntity<?> rankConstructors(@RequestParam List<String> rankedCompetitors) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		try {
			List<Constructor> validationList = db.getConstructorsYear(year);
			Set<Constructor> competitors = new HashSet<>(validationList);
			List<Constructor> guessedConstructors = rankedCompetitors.stream()
					.map(constructor -> new Constructor(constructor, db))
					.toList();
			String error = validateGuessList(guessedConstructors, competitors);
			if (error != null) {
				logger.warn(error);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			int position = 1;
			UUID id = userService.getUser().id();
			for (Constructor constructor : guessedConstructors) {
				db.insertConstructorsYearGuess(id, constructor, year, position);
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
		Category category = new Category("TENTH", db);
		return handleGetChooseDriver(category);
	}

	@PostMapping("/tenth")
	@Transactional
	public ResponseEntity<?> guessTenth(@RequestParam String driver) {
		Category category = new Category("TENTH", db);
		return handlePostChooseDriver(driver, category);
	}

	@GetMapping("/first")
	public ResponseEntity<CutoffCompetitorsSelected<Driver>> guessWinner() {
		Category category = new Category("FIRST", db);
		return handleGetChooseDriver(category);
	}

	@PostMapping("/first")
	@Transactional
	public ResponseEntity<?> guessWinner(@RequestParam String driver) {
		Category category = new Category("FIRST", db);
		return handlePostChooseDriver(driver, category);
	}

	private ResponseEntity<CutoffCompetitorsSelected<Driver>> handleGetChooseDriver(Category category) {
		try {
			Race race = db.getRaceFromId(getRaceIdToGuess());
			long timeLeftToGuess = db.getTimeLeftToGuessRace(race.id());
			List<ColoredCompetitor<Driver>> drivers = db.getDriversFromStartingGridWithColors(race.id());
			UUID id = userService.getUser().id();
			try {
				Driver driver = db.getGuessedDriverPlace(race.id(), category, id);
				CutoffCompetitorsSelected<Driver> res = new CutoffCompetitorsSelected<>(drivers, driver, timeLeftToGuess, race);
				return new ResponseEntity<>(res, HttpStatus.OK);
			} catch (EmptyResultDataAccessException e) {
				CutoffCompetitorsSelected<Driver> res = new CutoffCompetitorsSelected<>(drivers, null, timeLeftToGuess, race);
				return new ResponseEntity<>(res, HttpStatus.OK);
			}
		} catch (NoAvailableRaceException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	private ResponseEntity<?> handlePostChooseDriver(String driver, Category category) {
		try {
			RaceId raceId = getRaceIdToGuess();
			Driver validDriver = new Driver(driver, db);
			Set<Driver> driversCheck = new HashSet<>(db.getDriversFromStartingGrid(raceId));
			if (!driversCheck.contains(validDriver)) {
				logger.warn("'{}', invalid winner driver inputted by user.", driver);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			UUID id = userService.getUser().id();
			db.addDriverPlaceGuess(id, raceId, validDriver, category);
			logger.info("User '{}' guessed on category '{}' on race '{}'", id, category, raceId);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (NoAvailableRaceException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (InvalidDriverException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	private RaceId getRaceIdToGuess() throws NoAvailableRaceException {
		try {
			RaceId raceId = db.getCurrentRaceIdToGuess();
			if (!cutoff.isAbleToGuessRace(raceId)) {
				throw new NoAvailableRaceException("Cutoff has been passed");
			}
			return raceId;
		} catch (EmptyResultDataAccessException e) {
			throw new NoAvailableRaceException("Currently there are no available races");
		}
	}

	@GetMapping("/flag")
	public ResponseEntity<CutoffFlags> guessFlags() {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		Flags flags = db.getFlagGuesses(userService.getUser().id(), year);
		CutoffFlags res = new CutoffFlags(flags, timeLeftToGuess);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@PostMapping("/flag")
	@Transactional
	public ResponseEntity<?> guessFlags(@RequestParam int yellow, @RequestParam int red,
			@RequestParam int safetyCar) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Flags flags = new Flags(yellow, red, safetyCar);
		if (!flags.hasValidValues()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		UUID id = userService.getUser().id();
		db.addFlagGuesses(id, year, flags);
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
