package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.Arrays;
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

import no.vebb.f1.database.Database;
import no.vebb.f1.importing.Importer;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.CutoffRace;
import no.vebb.f1.util.Table;

@Controller
@RequestMapping("/admin/season/{year}/manage")
public class ManageSeasonController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private Database db;

	private JdbcTemplate jdbcTemplate;

	public ManageSeasonController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public String manageRacesInSeason(@RequestParam(value = "success", required = false) Boolean success,
			@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
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
		List<CutoffRace> races = db.getCutoffRaces(year);
		model.addAttribute("races", races);
		model.addAttribute("title", year);
		model.addAttribute("year", year);
		return "manageSeason";
	}

	@GetMapping("/{raceId}")
	public String manageRacesInSeason(@PathVariable("raceId") int raceId, @PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean isValidRaceId = db.isValidRaceInSeason(raceId, year);
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}

		List<Table> tables = new ArrayList<>();
		tables.add(getStartingGridTable(raceId));
		tables.add(getRaceResultTable(raceId));
		tables.add(getDriverStandingsTable(raceId));
		tables.add(getConstructorStandingsTable(raceId));

		model.addAttribute("tables", tables);
		model.addAttribute("title", year);
		return "tables";
	}

	private Table getStartingGridTable(int raceId) {
		final String getStartingGrid = """
				SELECT position, driver
				FROM StartingGrid
				WHERE race_number = ?
				ORDER BY position ASC
				""";
		List<String> header = Arrays.asList("Plass", "Sjåfør");
		List<List<String>> body = new ArrayList<>();
		List<Map<String, Object>> startingGrid = jdbcTemplate.queryForList(getStartingGrid, raceId);
		for (Map<String, Object> row : startingGrid) {
			String position = String.valueOf((int) row.get("position"));
			String driver = (String) row.get("driver");
			body.add(Arrays.asList(position, driver));
		}

		return new Table("Starting grid", header, body);
	}

	private Table getRaceResultTable(int raceId) {
		final String getRaceResult = """
			SELECT position, driver, points
			FROM RaceResult
			WHERE race_number = ?
			ORDER BY finishing_position ASC
			""";
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<Map<String, Object>> raceResult = jdbcTemplate.queryForList(getRaceResult, raceId);
		for (Map<String, Object> row : raceResult) {
			String position = (String) row.get("position");
			String driver = (String) row.get("driver");
			String points = (String) row.get("points");;
			body.add(Arrays.asList(position, driver, points));
		}

		return new Table("Race result", header, body);
	}

	private Table getDriverStandingsTable(int raceId) {
		final String getDriverStandings = """
			SELECT position, driver, points
			FROM DriverStandings
			WHERE race_number = ?
			ORDER BY position ASC
			""";
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<Map<String, Object>> standings = jdbcTemplate.queryForList(getDriverStandings, raceId);
		for (Map<String, Object> row : standings) {
			String position = String.valueOf((int) row.get("position"));
			String driver = (String) row.get("driver");
			String points = (String) row.get("points");;
			body.add(Arrays.asList(position, driver, points));
		}

		return new Table("Driver standings", header, body);
	}

	private Table getConstructorStandingsTable(int raceId) {
		final String getConstructorStandings = """
			SELECT position, constructor, points
			FROM ConstructorStandings
			WHERE race_number = ?
			ORDER BY position ASC
			""";
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<Map<String, Object>> standings = jdbcTemplate.queryForList(getConstructorStandings, raceId);
		for (Map<String, Object> row : standings) {
			String position = String.valueOf((int) row.get("position"));
			String constructor = (String) row.get("constructor");
			String points = (String) row.get("points");;
			body.add(Arrays.asList(position, constructor, points));
		}

		return new Table("Constructor standings", header, body);
	}

	@PostMapping("/reload")
	public String reloadRace(@RequestParam("id") int raceId, @PathVariable("year") int year) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean isValidRaceId = db.isValidRaceInSeason(raceId, year);
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}

		Importer importer = new Importer(db);
		importer.importRaceData(raceId);

		return "redirect:/admin/season/" + year + "/manage/" + raceId;
	}

	@PostMapping("/move")
	public String changeRaceOrder(@PathVariable int year, @RequestParam("id") int raceId,
			@RequestParam("newPosition") int position) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean isValidRaceId = db.isValidRaceInSeason(raceId, year);
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
		int maxPos = db.getMaxRaceOrderPosition(year);
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
		Importer importer = new Importer(db);
		importer.importData();
		return "redirect:/admin/season/" + year + "/manage?success=true";
	}

	@PostMapping("/delete")
	public String deleteRace(@PathVariable int year, @RequestParam("id") int raceId) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean isValidRaceId = db.isValidRaceInSeason(raceId, year);
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

	@PostMapping("/add")
	public String addRace(@PathVariable int year, @RequestParam("id") int raceId) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean isRaceIdInUse = db.isValidRaceInSeason(raceId, year);
		if (isRaceIdInUse) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
		Importer importer = new Importer(db);
		importer.importRaceName(raceId, year);
		importer.importData();
		return "redirect:/admin/season/" + year + "/manage?success=true";
	}
}
