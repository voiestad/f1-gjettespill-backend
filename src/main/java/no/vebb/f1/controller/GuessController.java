package no.vebb.f1.controller;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Flags;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidConstructorException;
import no.vebb.f1.util.exception.InvalidDriverException;
import no.vebb.f1.util.exception.NoAvailableRaceException;

@Controller
@RequestMapping("/guess")
public class GuessController {

	private static final Logger logger = LoggerFactory.getLogger(GuessController.class);

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@Autowired
	private Cutoff cutoff;

	/**
	 * Handles GET requests for /guess. Gives a list of links to the categories that
	 * are currently available for guessing.
	 */
	@GetMapping
	public String guess(@RequestParam(value = "success", required = false) Boolean success, Model model) {
		if (success != null) {
			if (success) {
				model.addAttribute("successMessage", "Tippingen din ble lagret");
			} else {
				model.addAttribute("successMessage",
						"Tippingen feilet, vennligst prøv igjen eller kontakt administrator");
			}
		}
		model.addAttribute("title", "Velg kategori");
		Map<String, String> linkMap = new LinkedHashMap<>();
		if (cutoff.isAbleToGuessCurrentYear()) {
			linkMap.put("Ranger sjåfører", "/guess/drivers");
			linkMap.put("Ranger konstruktører", "/guess/constructors");
			linkMap.put("Tipp antall", "/guess/flags");
		}
		if (isRaceToGuess()) {
			linkMap.put("Tipp 1.plass", "/guess/winner");
			linkMap.put("Tipp 10.plass", "/guess/tenth");
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	/**
	 * Handles GET requests for /guess/drivers. If it is still possible to guess,
	 * gives a list of drivers to rank and the time left to guess.
	 */
	@GetMapping("/drivers")
	public String rankDrivers(Model model) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess";
		}
		UUID id = userService.loadUser().get().id;
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		List<Driver> competitors = db.getDriversGuess(id, year);
		model.addAttribute("timeLeftToGuess", timeLeftToGuess);
		model.addAttribute("competitors", competitors);
		model.addAttribute("title", "Ranger sjåførene");
		model.addAttribute("type", "drivers");
		return "ranking";
	}

	/**
	 * Handles POST requests for /guess/drivers. If it is still possible to guess,
	 * and input guess is valid, adds the guesses to the database.
	 */
	@PostMapping("/drivers")
	public String rankDrivers(@RequestParam List<String> rankedCompetitors, Model model) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess?success=false";
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
				return "redirect:/guess?success=false";
			}
			int position = 1;
			UUID id = userService.loadUser().get().id;
			for (Driver driver : guessedDrivers) {
				db.insertDriversYearGuess(id, driver, year, position);
				position++;
			}
			logger.info("User '{}' guessed on '{}' on year '{}'", id, "driver", year);
		} catch (InvalidDriverException e) {
			return "redirect:/guess?success=false";
		}
		return "redirect:/guess?success=true";
	}

	/**
	 * Handles GET requests for /guess/constructors. If it is still possible to
	 * guess,
	 * gives a list of constructors to rank and the time left to guess.
	 */
	@GetMapping("/constructors")
	public String rankConstructors(Model model) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess";
		}
		UUID id = userService.loadUser().get().id;
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		List<Constructor> competitors = db.getConstructorsGuess(id, year);
		model.addAttribute("timeLeftToGuess", timeLeftToGuess);
		model.addAttribute("competitors", competitors);
		model.addAttribute("title", "Ranger konstruktørene");
		model.addAttribute("type", "constructors");
		return "ranking";
	}

	/**
	 * Handles POST requests for /guess/constructors. If it is still possible to
	 * guess,
	 * and input guess is valid, adds the guesses to the database.
	 */
	@PostMapping("/constructors")
	public String rankConstructors(@RequestParam List<String> rankedCompetitors, Model model) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess?success=false";
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
				return "redirect:/guess?success=false";
			}
			int position = 1;
			UUID id = userService.loadUser().get().id;
			for (Constructor constructor : guessedConstructors) {
				db.insertConstructorsYearGuess(id, constructor, year, position);
				position++;
			}
			logger.info("User '{}' guessed on '{}' on year '{}'", id, "constructor", year);
		} catch (InvalidConstructorException e) {
			return "redirect:/guess?success=false";
		}
		return "redirect:/guess?success=true";
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

	/**
	 * Handles GET requests for /guess/tenth. If it is possible to guess, gives a
	 * list of drivers that can be chosen.
	 */
	@GetMapping("/tenth")
	public String guessTenth(Model model) {
		model.addAttribute("title", "Tipp 10.plass");
		model.addAttribute("type", "tenth");
		Category category = new Category("TENTH", db);
		return handleGetChooseDriver(model, category);
	}

	/**
	 * Handles POST requests for /guess/tenth. If it is possible to guess, gives a
	 * and input driver is valid, adds the guess to the database.
	 */
	@PostMapping("/tenth")
	public String guessTenth(@RequestParam String driver, Model model) {
		Category category = new Category("TENTH", db);
		return handlePostChooseDriver(model, driver, category);
	}

	/**
	 * Handles GET requests for /guess/winner. If it is possible to guess, gives a
	 * list of drivers that can be chosen.
	 */
	@GetMapping("/winner")
	public String guessWinner(Model model) {
		model.addAttribute("title", "Tipp Vinneren");
		model.addAttribute("type", "winner");
		Category category = new Category("FIRST", db);
		return handleGetChooseDriver(model, category);
	}

	/**
	 * Handles POST requests for /guess/winner. If it is possible to guess, gives a
	 * and input driver is valid, adds the guess to the database.
	 */
	@PostMapping("/winner")
	public String guessWinner(@RequestParam String driver, Model model) {
		Category category = new Category("FIRST", db);
		return handlePostChooseDriver(model, driver, category);
	}

	private String handleGetChooseDriver(Model model, Category category) {
		try {
			RaceId raceId = getRaceIdToGuess();

			long timeLeftToGuess = db.getTimeLeftToGuessRace(raceId);
			model.addAttribute("timeLeftToGuess", timeLeftToGuess);

			List<Driver> drivers = db.getDriversFromStartingGrid(raceId);
			model.addAttribute("items", drivers);
			UUID id = userService.loadUser().get().id;
			String driver = db.getGuessedDriverPlace(raceId, category, id).toString();
			model.addAttribute("guessedDriver", driver);
		} catch (NoAvailableRaceException e) {
			return "redirect:/guess";
		} catch (EmptyResultDataAccessException e) {
			model.addAttribute("guessedDriver", "");
		}

		return "chooseDriver";
	}

	private String handlePostChooseDriver(Model model, String driver, Category category) {
		try {
			RaceId raceId = getRaceIdToGuess();
			Driver validDriver = new Driver(driver, db);
			Set<Driver> driversCheck = new HashSet<>(db.getDriversFromStartingGrid(raceId));
			if (!driversCheck.contains(validDriver)) {
				logger.warn("'{}', invalid winner driver inputted by user.", driver);
				return "redirect:/guess?success=false";
			}
			UUID id = userService.loadUser().get().id;
			db.addDriverPlaceGuess(id, raceId, validDriver, category);
			logger.info("User '{}' guessed on category '{}' on race '{}'", id, category, raceId);
			return "redirect:/guess?success=true";

		} catch (NoAvailableRaceException e) {
		} catch (InvalidDriverException e) {
		}
		return "redirect:/guess?success=false";
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

	/**
	 * Handles GET requests for /guess/flags. If it is possible to guess, gives the
	 * users previous guesses.
	 */
	@GetMapping("/flags")
	public String guessFlags(Model model) {
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess";
		}
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		model.addAttribute("timeLeftToGuess", timeLeftToGuess);
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		Flags flags = db.getFlagGuesses(userService.loadUser().get().id, year);
		model.addAttribute("flags", flags);
		return "guessFlags";
	}

	/**
	 * Handles POST requests for /guess/flags. If it is possible to guess and values
	 * are valid, adds the guesses to the database.
	 */
	@PostMapping("/flags")
	public String guessFlags(@RequestParam int yellow, @RequestParam int red,
			@RequestParam int safetyCar, Model model) {
		Flags flags = new Flags(yellow, red, safetyCar);
		if (!flags.hasValidValues()) {
			model.addAttribute("error", "Verdier kan ikke være negative");
			return "guessFlags";
		}
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess?success=false";
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		UUID id = userService.loadUser().get().id;
		db.addFlagGuesses(id, year, flags);
		logger.info("User '{}' guessed on flags on year '{}'", id, year);
		return "redirect:/guess?success=true";
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
