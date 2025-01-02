package no.vebb.f1.controller;

import java.time.Instant;
import java.time.Year;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/guess")
public class RankingController {

	private JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(RankingController.class);

	@Autowired
	private UserService userService;

	public RankingController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

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
		linkMap.put("Ranger sjåfører", "/guess/drivers");
		linkMap.put("Ranger konstruktører", "/guess/constructors");
		linkMap.put("Tipp antall", "/guess/flags");
		linkMap.put("Tipp 1.plass", "/guess/winner");
		linkMap.put("Tipp 10.plass", "/guess/tenth");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/drivers")
	public String rankDrivers(Model model) {
		final String getDriversSql = "SELECT driver, position, year FROM DriverYear WHERE year = ? ORDER BY position ASC";
		final String getGuessedSql = "SELECT position, driver FROM DriverGuess WHERE guesser = ? ORDER BY position ASC";
		final String competitorColName = "driver";
		model.addAttribute("title", "Ranger sjåførene");
		model.addAttribute("type", "drivers");
		return handleRankGet(model, getDriversSql, getGuessedSql, competitorColName);
	}

	@PostMapping("/drivers")
	public String rankDrivers(@RequestParam List<String> rankedCompetitors, Model model) {
		final String getDriversSql = "SELECT driver, year FROM DriverYear WHERE year = ?";
		final String addRowDriver = "REPLACE INTO DriverGuess (guesser, driver, year, position) values (?, ?, ?, ?)";
		final String competitorColName = "driver";
		return handleRankPost(model, rankedCompetitors, getDriversSql, addRowDriver, competitorColName);
	}

	@GetMapping("/constructors")
	public String rankConstructors(Model model) {
		final String getConstructorsSql = "SELECT constructor, position, year FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		final String getGuessedSql = "SELECT position, constructor FROM ConstructorGuess WHERE guesser = ? ORDER BY position ASC";
		final String competitorColName = "constructor";
		model.addAttribute("title", "Ranger konstruktørene");
		model.addAttribute("type", "constructors");
		return handleRankGet(model, getConstructorsSql, getGuessedSql, competitorColName);
	}

	@PostMapping("/constructors")
	public String rankConstructors(@RequestParam List<String> rankedCompetitors, Model model) {
		final String getConstructorSql = "SELECT constructor, year FROM ConstructorYear WHERE year = ?";
		final String addRowConstructor = "REPLACE INTO ConstructorGuess (guesser, constructor, year, position) values (?, ?, ?, ?)";
		final String competitorColName = "constructor";
		return handleRankPost(model, rankedCompetitors, getConstructorSql, addRowConstructor, competitorColName);
	}

	private String handleRankGet(Model model, String getCompetitorsSql, String getGuessedSql, String competitorColName) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!isAbleToGuessSeason()) {
			return "redirect:/guess"; 
		}
		List<String> competitors = jdbcTemplate.query(getGuessedSql, (rs, rowNum) -> rs.getString(competitorColName), user.get().id);
		if (competitors.size() == 0) {
			competitors = jdbcTemplate.query(getCompetitorsSql, (rs, rowNum) -> rs.getString(competitorColName), getCurrentYear());
		}
		model.addAttribute("competitors", competitors);
		return "ranking";
	}

	private String handleRankPost(Model model, List<String> rankedCompetitors, String getCompetitorsSql, String addCompetitorsSql, String competitorColName) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!isAbleToGuessSeason()) {
			return "redirect:/guess?success=false"; 
		}
		Set<String> competitors = new HashSet<>((jdbcTemplate.query(getCompetitorsSql, (rs, rowNum) -> rs.getString(competitorColName), getCurrentYear())));
		String error = validateGuessList(rankedCompetitors, competitors);
		if (error != null) {
			logger.warn(error);
			return "redirect:/guess?success=false";
		}
		int position = 1;
		for (String competitor : rankedCompetitors) {
			jdbcTemplate.update(addCompetitorsSql, user.get().id, competitor, getCurrentYear(), position);
			position++;
		}
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
		return handleGetChooseDriver(model, "TENTH");
	}

	@PostMapping("/tenth")
	public String guessTenth(@RequestParam String driver, Model model) {
		return handlePostChooseDriver(model, driver, "TENTH");
	}

	@GetMapping("/winner")
	public String guessWinner(Model model) {
		model.addAttribute("title", "Tipp Vinneren");
		model.addAttribute("type", "winner");
		return handleGetChooseDriver(model, "FIRST");
	}

	@PostMapping("/winner")
	public String guessWinner(@RequestParam String driver, Model model) {
		return handlePostChooseDriver(model, driver, "FIRST");
	}

	private String handleGetChooseDriver(Model model, String category) {
		final String getPreviousGuessSql = "SELECT driver FROM DriverPlaceGuess WHERE race_number = ? AND category = ?";
		final String getDriversFromGrid = "SELECT driver FROM StartingGrid WHERE race_number = ? ORDER BY position ASC";
		try {
			int raceNumber = getRaceIdToGuess();

			List<String> drivers = jdbcTemplate.query(getDriversFromGrid, (rs, rowNum) -> rs.getString("driver"), raceNumber);
			model.addAttribute("items", drivers);

			String driver = jdbcTemplate.queryForObject(getPreviousGuessSql, (rs, rowNum) -> rs.getString("driver"), raceNumber, category);
			model.addAttribute("guessedDriver", driver);
		} catch (NoAvailableRaceException e) {
			return "redirect:/guess";
		} catch (EmptyResultDataAccessException e) {
			model.addAttribute("guessedDriver", "");
		}

		return "chooseDriver";
	}

	private String handlePostChooseDriver(Model model, String driver, String category) {
		final String insertGuessSql = "REPLACE INTO DriverPlaceGuess (guesser, race_number, driver, category) values (?, ?, ?, ?)";
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		try {
			int raceNumber = getRaceIdToGuess();

			String sql = "SELECT driver FROM StartingGrid WHERE race_number = ?";
			Set<String> driversCheck = new HashSet<>((jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("driver"), raceNumber)));
			if (!driversCheck.contains(driver)) {
				logger.warn("'{}', invalid winner driver inputted by user.", driver);
				return "redirect:/guess?success=false";
			}
			jdbcTemplate.update(insertGuessSql, user.get().id, raceNumber, driver, category);
			logger.info("Guessed '{}' on {}", driver, category);
			return "redirect:/guess?success=true";

		} catch (NoAvailableRaceException e) {
			return "redirect:/guess";
		}
	}

	private int getRaceIdToGuess() throws NoAvailableRaceException {
		final String getRaceId = """
				SELECT race_number
				FROM StartingGrid sg
				WHERE sg.race_number NOT IN (
					SELECT rr.race_number 
					FROM RaceResult rr
				) 
				""";
		try {
			int id = jdbcTemplate.queryForObject(getRaceId, Integer.class);
			final String getCutoff = "SELECT cutoff FROM RaceCutoff WHERE race_number = ?";
			Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, String.class, id));
			if (!isAbleToGuess(cutoff)) {
				throw new NoAvailableRaceException("Cutoff has been passed");
			}
			return id;
		} catch (EmptyResultDataAccessException e) {
			throw new NoAvailableRaceException("Currently there are no available races");
		}
	}

	private class NoAvailableRaceException extends Exception {
		public NoAvailableRaceException(String e) {
			super(e);
		}
	}

	@GetMapping("/flags")
	public String guessFlags(Model model) {
		Optional<User> user = userService.loadUser();
		if (!user.isPresent()) {
			return "redirect:/";
		}
		if (!isAbleToGuessSeason()) {
			return "redirect:/guess"; 
		}
		final String sql = "SELECT flag, amount FROM FlagGuess WHERE guesser = ? AND year = ?";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, user.get().id, getCurrentYear());
		Flags flags = new Flags();
		for (Map<String, Object> row : sqlRes) {
			String flag = (String) row.get("flag");
			int amount = (int) row.get("amount");
			switch (flag) {
				case "Yellow Flag":
					flags.yellow = amount;
					break;
				case "Red Flag":
					flags.red = amount;
					break;
				case "Safety Car":
					flags.safetyCar = amount;
					break;
			}
		}
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
		if (!isAbleToGuessSeason()) {
			return "redirect:/guess?success=false"; 
		}
		final String sql = "REPLACE INTO FlagGuess (guesser, flag, year, amount) values (?, ?, ?, ?)";
		jdbcTemplate.update(sql, user.get().id, "Yellow Flag", getCurrentYear(), flags.yellow);
		jdbcTemplate.update(sql, user.get().id, "Red Flag", getCurrentYear(), flags.red);
		jdbcTemplate.update(sql, user.get().id, "Safety Car", getCurrentYear(), flags.safetyCar);
		logger.info("Guessed '{}' yellow flags, '{}' red flags and '{}' safety cars", flags.yellow, flags.red,
				flags.safetyCar);
		return "redirect:/guess?success=true";
	}

	private int getCurrentYear() {
		return Year.now().getValue();
	}

	private boolean isAbleToGuessSeason() {
		final String existCheck = "SELECT COUNT(*) FROM YearCutoff WHERE year = ?";
		boolean yearExist = jdbcTemplate.queryForObject(existCheck, Integer.class, getCurrentYear()) > 0;
		if (!yearExist) {
			return false;
		}
		final String getCutoff = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		Instant cutoff = Instant.parse(jdbcTemplate.queryForObject(getCutoff, (rs, rowNum) -> rs.getString("cutoff"), getCurrentYear()));

		return isAbleToGuess(cutoff);
	}

	private boolean isAbleToGuess(Instant cutoff) {
		Instant now = Instant.now();
		return cutoff.compareTo(now) > 0;
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

	class Flags {
		public int yellow;
		public int red;
		public int safetyCar;

		public boolean hasValidValues() {
			return yellow >= 0 && red >= 0 && safetyCar >= 0;
		}

		public Flags() {
		}

		public Flags(int yellow, int red, int safetyCar) {
			this.yellow = yellow;
			this.red = red;
			this.safetyCar = safetyCar;
		}
	}
}
