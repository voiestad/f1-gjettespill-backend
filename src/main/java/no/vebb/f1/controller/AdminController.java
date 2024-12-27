package no.vebb.f1.controller;

import java.time.Instant;
import java.util.ArrayList;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.importing.Importer;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	public AdminController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping()
	public String adminHome(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Admin Portal");
		Map<String, String> linkMap = new LinkedHashMap<>();
		linkMap.put("Registrer flagg", "/admin/flag");
		linkMap.put("Administrer sesonger", "/admin/season");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/season")
	public String seasonAdminOverview(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Administrer sesonger");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT DISTINCT year FROM RaceOrder ORDER BY year DESC";
		List<Integer> years = jdbcTemplate.query(sql, (rs, rowNum) -> Integer.parseInt(rs.getString("year")));
		for (Integer year : years) {
			linkMap.put(String.valueOf(year), "/admin/season/" + year);
		}
		linkMap.put("Legg til ny sesong", "/admin/season/add");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/season/{year}")
	public String seasonMenu(@RequestParam(value = "success", required = false) Boolean success,
			@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		model.addAttribute("title", year);
		Map<String, String> linkMap = new LinkedHashMap<>();
		String basePath = "/admin/season/" + year;
		linkMap.put("Endring av løp", basePath + "/manage");
		linkMap.put("Frister", basePath + "/cutoff");
		linkMap.put("F1 Deltakere", basePath + "/competitors");

		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/season/{year}/manage")
	public String manageRacesInSeason(@RequestParam(value = "success", required = false) Boolean success,
			@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		if (success != null) {
			if (success) {
				model.addAttribute("successMessage", "Endringen ble lagret");
			} else {
				model.addAttribute("successMessage", "Endringen feilet");
			}
		}
		List<Race> races = new ArrayList<>();
		final String getRaces = """
				SELECT r.id AS id, r.name AS name, ro.position AS position
				FROM Race r
				JOIN RaceOrder ro ON r.id = ro.id
				WHERE ro.year = ?
				""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRaces, year);
		for (Map<String, Object> row : sqlRes) {
			int position = (int) row.get("position");
			String name = (String) row.get("name");
			int id = (int) row.get("id");
			races.add(new Race(position, name, id));
		}
		model.addAttribute("races", races);
		model.addAttribute("title", year);
		model.addAttribute("year", year);
		return "manageSeason";
	}

	@PostMapping("/season/{year}/manage/move")
	public String changeRaceOrder(@PathVariable int year, @RequestParam("id") int raceId,
			@RequestParam("newPosition") int position) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		final String validateRaceId = "SELECT COUNT(*) FROM RaceOrder WHERE year = ? AND id = ?";
		boolean isValidRaceId = jdbcTemplate.queryForObject(validateRaceId, Integer.class, year, raceId) > 0;
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
		final String maxPosSql = "SELECT MAX(position) FROM RaceOrder WHERE year = ?";
		int maxPos = jdbcTemplate.queryForObject(maxPosSql, Integer.class, year);
		boolean isPosOutOfBounds = position < 1 || position > maxPos;
		if (isPosOutOfBounds) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
		final String getRacesSql = "SELECT * FROM RaceOrder WHERE year = ? AND id != ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRacesSql, year, raceId);
		final String removeOldOrderSql = "DELETE FROM RaceOrder WHERE year = ?";
		jdbcTemplate.update(removeOldOrderSql, year);
		int currentPos = 1;
		final String insertRaceSql = "INSERT INTO RaceOrder (id, year, position) VALUES (?, ?, ?)";
		for (Map<String, Object> row : sqlRes) {
			if (currentPos == position) {
				jdbcTemplate.update(insertRaceSql, raceId, year, position);
				currentPos++;
			}
			jdbcTemplate.update(insertRaceSql, (int) row.get("id"), year, currentPos);
			currentPos++;
		}
		if (currentPos == position) {
			jdbcTemplate.update(insertRaceSql, raceId, year, position);
		}
		Importer importer = new Importer(jdbcTemplate);
		importer.importData();
		return "redirect:/admin/season/" + year + "/manage?success=true";
	}

	@PostMapping("/season/{year}/manage/delete")
	public String deleteRace(@PathVariable int year, @RequestParam("id") int raceId) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		final String validateRaceId = "SELECT COUNT(*) FROM RaceOrder WHERE year = ? AND id = ?";
		boolean isValidRaceId = jdbcTemplate.queryForObject(validateRaceId, Integer.class, year, raceId) > 0;
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
		final String deleteRace = "DELETE FROM Race WHERE id = ?";
		jdbcTemplate.update(deleteRace, raceId);

		final String getRacesSql = "SELECT * FROM RaceOrder WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRacesSql, year);
		final String removeOldOrderSql = "DELETE FROM RaceOrder WHERE year = ?";
		jdbcTemplate.update(removeOldOrderSql, year);
		int currentPos = 1;
		final String insertRaceSql = "INSERT INTO RaceOrder (id, year, position) VALUES (?, ?, ?)";
		for (Map<String, Object> row : sqlRes) {
			jdbcTemplate.update(insertRaceSql, (int) row.get("id"), year, currentPos);
			currentPos++;
		}
		return "redirect:/admin/season/" + year + "/manage?success=true";
	}

	@PostMapping("/season/{year}/manage/add")
	public String addRace(@PathVariable int year, @RequestParam("id") int raceId) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		final String validateRaceId = "SELECT COUNT(*) FROM RaceOrder WHERE id = ?";
		boolean isRaceIdInUse = jdbcTemplate.queryForObject(validateRaceId, Integer.class, raceId) > 0;
		if (isRaceIdInUse) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
		Importer importer = new Importer(jdbcTemplate);
		importer.importRaceName(raceId, year);
		importer.importData();
		return "redirect:/admin/season/" + year + "/manage?success=true";
	}

	@GetMapping("/season/add")
	public String addSeasonForm() {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		return "addSeason";
	}

	@PostMapping("/season/add")
	public String addSeason(@RequestParam("year") int year, @RequestParam("start") int start,
			@RequestParam("end") int end, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String getRaceNameSql = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isAlreadyAdded = jdbcTemplate.queryForObject(getRaceNameSql, Integer.class, year) > 0;
		if (isAlreadyAdded) {
			model.addAttribute("error", String.format("Sesongen %d er allerede lagt til", year));
			return "addSeason";
		}
		if (start > end) {
			model.addAttribute("error", "Starten av året kan ikke være etter slutten av året");
			return "addSeason";
		}

		List<Integer> races = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			races.add(i);
		}

		Importer importer = new Importer(jdbcTemplate);
		importer.importRaceNames(races, year);
		importer.importData();
		return "redirect:/admin/season";
	}

	@GetMapping("/season/{year}/competitors")
	public String addSeasonCompetitorsForm(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String getDrivers = "SELECT * FROM DriverYear WHERE year = ? ORDER BY position ASC";
		final String getConstructors = "SELECT * FROM ConstructorYear WHERE year = ? ORDER BY position ASC";

		List<String> drivers = new ArrayList<>();
		List<Map<String, Object>> sqlResDrivers = jdbcTemplate.queryForList(getDrivers, year);
		for (Map<String, Object> row : sqlResDrivers) {
			drivers.add((String) row.get("driver"));
		}

		List<String> constructors = new ArrayList<>();
		List<Map<String, Object>> sqlResConstructors = jdbcTemplate.queryForList(getConstructors, year);
		for (Map<String, Object> row : sqlResConstructors) {
			constructors.add((String) row.get("constructor"));
		}

		model.addAttribute("title", year);
		model.addAttribute("year", year);
		model.addAttribute("drivers", drivers);
		model.addAttribute("constructors", constructors);
		return "addCompetitors";
	}

	@PostMapping("/season/{year}/competitors/addDriver")
	public String addDriverToSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String insertDriverYear = "INSERT OR IGNORE INTO Driver (name) VALUES (?)";
		jdbcTemplate.update(insertDriverYear, driver);
		final String getMaxPos = "SELECT COALESCE(MAX(position), 0) FROM DriverYear WHERE year = ?";
		int position = jdbcTemplate.queryForObject(getMaxPos, Integer.class, year) + 1;
		final String addDriverYear = "INSERT INTO DriverYear (driver, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(addDriverYear, driver, year, position);
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/season/{year}/competitors/addConstructor")
	public String addConstructorToSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String insertConstructor = "INSERT OR IGNORE INTO Constructor (name) VALUES (?)";
		jdbcTemplate.update(insertConstructor, constructor);
		final String getMaxPos = "SELECT COALESCE(MAX(position), 0) FROM ConstructorYear WHERE year = ?";
		int position = jdbcTemplate.queryForObject(getMaxPos, Integer.class, year) + 1;
		final String addConstructorYear = "INSERT INTO ConstructorYear (constructor, year, position) VALUES (?, ?, ?)";
		jdbcTemplate.update(addConstructorYear, constructor, year, position);
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/season/{year}/competitors/deleteDriver")
	public String removeDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String existCheck = "SELECT COUNT(*) FROM DriverYear WHERE year = ? AND driver = ?";
		boolean driverExists = jdbcTemplate.queryForObject(existCheck, Integer.class, year, driver) > 0;
		if (!driverExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		final String deleteDriver = "DELETE FROM DriverYear WHERE year = ? AND driver = ?";
		jdbcTemplate.update(deleteDriver, year, driver);

		final String getAllDrivers = "SELECT * FROM DriverYear WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getAllDrivers, year);

		final String deleteAllDrivers = "DELETE FROM DriverYear WHERE year = ?";
		jdbcTemplate.update(deleteAllDrivers, year);

		int position = 1;
		final String addDriverYear = "INSERT INTO DriverYear (driver, year, position) VALUES (?, ?, ?)";
		for (Map<String, Object> row : sqlRes) {
			String currentDriver = (String) row.get("driver");
			jdbcTemplate.update(addDriverYear, currentDriver, year, position);
			position++;
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/season/{year}/competitors/deleteConstructor")
	public String removeConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String existCheck = "SELECT COUNT(*) FROM ConstructorYear WHERE year = ? AND constructor = ?";
		boolean constructorExists = jdbcTemplate.queryForObject(existCheck, Integer.class, year, constructor) > 0;
		if (!constructorExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		final String deleteConstructor = "DELETE FROM ConstructorYear WHERE year = ? AND constructor = ?";
		jdbcTemplate.update(deleteConstructor, year, constructor);

		final String getAllConstructors = "SELECT * FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getAllConstructors, year);

		final String deleteAllConstructors = "DELETE FROM ConstructorYear WHERE year = ?";
		jdbcTemplate.update(deleteAllConstructors, year);

		int position = 1;
		final String addConstructorYear = "INSERT INTO ConstructorYear (constructor, year, position) VALUES (?, ?, ?)";
		for (Map<String, Object> row : sqlRes) {
			String currentConstructor = (String) row.get("constructor");
			jdbcTemplate.update(addConstructorYear, currentConstructor, year, position);
			position++;
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/season/{year}/competitors/moveDriver")
	public String moveDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver,
			@RequestParam("newPosition") int position) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String existCheck = "SELECT COUNT(*) FROM DriverYear WHERE year = ? AND driver = ?";
		boolean driverExists = jdbcTemplate.queryForObject(existCheck, Integer.class, year, driver) > 0;
		if (!driverExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		final String maxPosSql = "SELECT MAX(position) FROM DriverYear WHERE year = ?";
		int maxPos = jdbcTemplate.queryForObject(maxPosSql, Integer.class, year);
		boolean isPosOutOfBounds = position < 1 || position > maxPos;
		if (isPosOutOfBounds) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		final String deleteDriver = "DELETE FROM DriverYear WHERE year = ? AND driver = ?";
		jdbcTemplate.update(deleteDriver, year, driver);

		final String getAllDrivers = "SELECT * FROM DriverYear WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getAllDrivers, year);

		final String deleteAllDrivers = "DELETE FROM DriverYear WHERE year = ?";
		jdbcTemplate.update(deleteAllDrivers, year);

		int currentPos = 1;
		final String addDriverYear = "INSERT INTO DriverYear (driver, year, position) VALUES (?, ?, ?)";
		for (Map<String, Object> row : sqlRes) {
			if (currentPos == position) {
				jdbcTemplate.update(addDriverYear, driver, year, currentPos);
				currentPos++;
			}
			String currentDriver = (String) row.get("driver");
			jdbcTemplate.update(addDriverYear, currentDriver, year, currentPos);
			currentPos++;
		}
		if (currentPos == position) {
			jdbcTemplate.update(addDriverYear, driver, year, currentPos);
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/season/{year}/competitors/moveConstructor")
	public String moveConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor, @RequestParam("newPosition") int position) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String existCheck = "SELECT COUNT(*) FROM ConstructorYear WHERE year = ? AND constructor = ?";
		boolean constructorExists = jdbcTemplate.queryForObject(existCheck, Integer.class, year, constructor) > 0;
		if (!constructorExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		final String maxPosSql = "SELECT MAX(position) FROM ConstructorYear WHERE year = ?";
		int maxPos = jdbcTemplate.queryForObject(maxPosSql, Integer.class, year);
		boolean isPosOutOfBounds = position < 1 || position > maxPos;
		if (isPosOutOfBounds) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		final String deleteConstructor = "DELETE FROM ConstructorYear WHERE year = ? AND constructor = ?";
		jdbcTemplate.update(deleteConstructor, year, constructor);

		final String getAllConstructors = "SELECT * FROM ConstructorYear WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getAllConstructors, year);

		final String deleteAllConstructors = "DELETE FROM ConstructorYear WHERE year = ?";
		jdbcTemplate.update(deleteAllConstructors, year);

		int currentPos = 1;
		final String addConstructorYear = "INSERT INTO ConstructorYear (constructor, year, position) VALUES (?, ?, ?)";
		for (Map<String, Object> row : sqlRes) {
			if (currentPos == position) {
				jdbcTemplate.update(addConstructorYear, constructor, year, currentPos);
				currentPos++;
			}
			String currentConstructor = (String) row.get("constructor");
			jdbcTemplate.update(addConstructorYear, currentConstructor, year, currentPos);
			currentPos++;
		}
		if (currentPos == position) {
			jdbcTemplate.update(addConstructorYear, constructor, year, currentPos);
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@GetMapping("/flag")
	public String flagChooseYear(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Velg år");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT DISTINCT year FROM RaceOrder ORDER BY year DESC";
		List<Integer> years = jdbcTemplate.query(sql, (rs, rowNum) -> Integer.parseInt(rs.getString("year")));
		for (Integer year : years) {
			linkMap.put(String.valueOf(year), "/admin/flag/" + year);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/flag/{year}")
	public String flagChooseRace(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Velg løp");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT r.name AS name, r.id AS id FROM Race r JOIN RaceOrder ro ON ro.id = r.id WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, year);
		int i = 1;
		for (Map<String, Object> row : sqlRes) {
			String name = (String) row.get("name");
			int id = (int) row.get("id");
			linkMap.put(i++ + ". " + name, "/admin/flag/" + year + "/" + id);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/flag/{year}/{id}")
	public String registrerFlags(@PathVariable("year") int year, @PathVariable("id") int raceId, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		List<String> flags = getFlags();
		model.addAttribute("flags", flags);
		model.addAttribute("raceId", raceId);

		List<RegisteredFlag> registeredFlags = new ArrayList<>();
		final String getRegisteredFlags = "SELECT flag, round, id FROM FlagStats WHERE race_number = ?";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRegisteredFlags, raceId);
		for (Map<String, Object> row : sqlRes) {
			String type = (String) row.get("flag");
			int round = (int) row.get("round");
			int id = (int) row.get("id");

			registeredFlags.add(new RegisteredFlag(type, round, id));
		}

		model.addAttribute("registeredFlags", registeredFlags);

		final String getRaceNameSql = "SELECT name FROM Race WHERE id = ?";
		String raceName = jdbcTemplate.queryForObject(getRaceNameSql, (rs, rowNum) -> rs.getString("name"), raceId);
		model.addAttribute("title", "Flagg " + raceName);
		return "noteFlags";
	}

	@PostMapping("/flag/add")
	public String registerFlag(@RequestParam("flag") String flag, @RequestParam("round") int round,
			@RequestParam("raceId") int raceId, @RequestParam("origin") String origin) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		Set<String> flags = new HashSet<>(getFlags());
		if (!flags.contains(flag)) {
			return "redirect:/";
		}

		final String sql = "INSERT INTO FlagStats (flag, race_number, round) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, flag, raceId, round);

		return "redirect:" + origin;
	}

	@PostMapping("/flag/delete")
	public String deleteFlag(@RequestParam("id") int id, @RequestParam("origin") String origin) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		final String sql = "DELETE FROM FlagStats WHERE id = ?";
		jdbcTemplate.update(sql, id);

		return "redirect:" + origin;
	}

	private List<String> getFlags() {
		final String sql = "SELECT name FROM Flag";
		return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));
	}

	class Race {
		public final int position;
		public final String name;
		public final int id;
		public final Instant cutoff;

		public Race(int position, String name, int id) {
			this(position, name, id, null);
		}

		public Race(int position, String name, int id, Instant cutoff) {
			this.position = position;
			this.name = name;
			this.id = id;
			this.cutoff = cutoff;
		}
	}

	class RegisteredFlag {
		public final String type;
		public final int round;
		public final int id;

		public RegisteredFlag(String type, int round, int id) {
			this.type = type;
			this.round = round;
			this.id = id;
		}
	}

}
