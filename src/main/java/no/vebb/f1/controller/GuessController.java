package no.vebb.f1.controller;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Category;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.Year;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.util.Flags;


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

	@GetMapping()
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

	@GetMapping("/drivers")
	public String rankDrivers(Model model) {
		model.addAttribute("title", "Ranger sjåførene");
		model.addAttribute("type", "drivers");
		return handleRankGet(model, "driver");
	}

	@PostMapping("/drivers")
	public String rankDrivers(@RequestParam List<String> rankedCompetitors, Model model) {
		return handleRankPost(model, rankedCompetitors, "driver");
	}

	@GetMapping("/constructors")
	public String rankConstructors(Model model) {
		model.addAttribute("title", "Ranger konstruktørene");
		model.addAttribute("type", "constructors");
		return handleRankGet(model, "constructor");
	}

	@PostMapping("/constructors")
	public String rankConstructors(@RequestParam List<String> rankedCompetitors, Model model) {
		return handleRankPost(model, rankedCompetitors, "constructor");
	}

	private String handleRankGet(Model model, String type) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess"; 
		}
		UUID id = user.get().id;
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		List<String> competitors = db.getCompetitorsGuess(type, id, year);
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		model.addAttribute("timeLeftToGuess", timeLeftToGuess);
		model.addAttribute("competitors", competitors);
		return "ranking";
	}

	private String handleRankPost(Model model, List<String> rankedCompetitors, String competitorType) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess?success=false"; 
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		Set<String> competitors = new HashSet<>(db.getCompetitorsYear(year, competitorType));
		String error = validateGuessList(rankedCompetitors, competitors);
		if (error != null) {
			logger.warn(error);
			return "redirect:/guess?success=false";
		}
		int position = 1;
		UUID id = user.get().id;
		for (String competitor : rankedCompetitors) {
			db.insertCompetitorsYearGuess(competitorType, id, competitor, year, position);
			position++;
		}
		logger.info("User '{}' guessed on '{}' on year '{}'", user.get().id, competitorType, year);
		return "redirect:/guess?success=true";
	}

	private String validateGuessList(List<String> guessed, Set<String> original) {
		Set<String> guessedSet = new HashSet<>();
		for (String competitor : guessed) {
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
	public String guessTenth(Model model) {
		model.addAttribute("title", "Tipp 10.plass");
		model.addAttribute("type", "tenth");
		Category category = new Category("TENTH", db);
		return handleGetChooseDriver(model, category);
	}

	@PostMapping("/tenth")
	public String guessTenth(@RequestParam String driver, Model model) {
		Category category = new Category("TENTH", db);
		return handlePostChooseDriver(model, driver, category);
	}

	@GetMapping("/winner")
	public String guessWinner(Model model) {
		model.addAttribute("title", "Tipp Vinneren");
		model.addAttribute("type", "winner");
		Category category = new Category("FIRST", db);
		return handleGetChooseDriver(model, category);
	}

	@PostMapping("/winner")
	public String guessWinner(@RequestParam String driver, Model model) {
		Category category = new Category("FIRST", db);
		return handlePostChooseDriver(model, driver, category);
	}

	private String handleGetChooseDriver(Model model, Category category) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		try {
			int raceNumber = getRaceIdToGuess();

			long timeLeftToGuess = db.getTimeLeftToGuessRace(raceNumber);
			model.addAttribute("timeLeftToGuess", timeLeftToGuess);

			List<String> drivers = db.getDriversFromStartingGrid(raceNumber);
			model.addAttribute("items", drivers);

			String driver = db.getGuessedDriverPlace(raceNumber, category, user.get().id);
			model.addAttribute("guessedDriver", driver);
		} catch (NoAvailableRaceException e) {
			return "redirect:/guess";
		} catch (EmptyResultDataAccessException e) {
			model.addAttribute("guessedDriver", "");
		}

		return "chooseDriver";
	}

	private String handlePostChooseDriver(Model model, String driver, Category category) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		try {
			int raceNumber = getRaceIdToGuess();
			Set<String> driversCheck = new HashSet<>(db.getDriversFromStartingGrid(raceNumber));
			if (!driversCheck.contains(driver)) {
				logger.warn("'{}', invalid winner driver inputted by user.", driver);
				return "redirect:/guess?success=false";
			}
			UUID id = user.get().id;
			db.addDriverPlaceGuess(id, raceNumber, driver, category);
			logger.info("User '{}' guessed on category '{}' on race '{}'", id, category, raceNumber);
			return "redirect:/guess?success=true";

		} catch (NoAvailableRaceException e) {
			return "redirect:/guess";
		}
	}

	private int getRaceIdToGuess() throws NoAvailableRaceException {
		try {
			int id = db.getCurrentRaceIdToGuess();
			if (!cutoff.isAbleToGuessRace(id)) {
				throw new NoAvailableRaceException("Cutoff has been passed");
			}
			return id;
		} catch (EmptyResultDataAccessException e) {
			throw new NoAvailableRaceException("Currently there are no available races");
		}
	}

	@GetMapping("/flags")
	public String guessFlags(Model model) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess"; 
		}
		long timeLeftToGuess = db.getTimeLeftToGuessYear();
		model.addAttribute("timeLeftToGuess", timeLeftToGuess);
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		Flags flags = db.getFlagGuesses(user.get().id, year);
		model.addAttribute("flags", flags);
		return "guessFlags";
	}

	@PostMapping("/flags")
	public String guessFlags(@RequestParam int yellow, @RequestParam int red,
			@RequestParam int safetyCar, Model model) {
		Flags flags = new Flags(yellow, red, safetyCar);
		if (!flags.hasValidValues()) {
			model.addAttribute("error", "Verdier kan ikke være negative");
			return "guessFlags";
		}
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!cutoff.isAbleToGuessCurrentYear()) {
			return "redirect:/guess?success=false"; 
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		db.addFlagGuesses(user.get().id, year, flags);
		logger.info("User '{}' guessed on flags on year '{}'", user.get().id, year);
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

	class PositionedItem implements Comparable<PositionedItem> {
		public final int pos;
		public final String value;

		public PositionedItem(int pos, String value) {
			this.pos = pos;
			this.value = value;
		}

		@Override
		public int compareTo(PositionedItem item) {
			if (pos > item.pos) {
				return 1;
			} else if (pos < item.pos) {
				return -1;
			}
			return 0;
		}
	}

}
