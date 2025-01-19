package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/admin/season/{year}/competitors")
public class SeasonCompetitorsController {

	@Autowired
	private UserService userService;

	private JdbcTemplate jdbcTemplate;

	public SeasonCompetitorsController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@GetMapping
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

	@PostMapping("/addDriver")
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

	@PostMapping("/addConstructor")
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

	@PostMapping("/deleteDriver")
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

	@PostMapping("/deleteConstructor")
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

	@PostMapping("/moveDriver")
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

	@PostMapping("/moveConstructor")
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
}
