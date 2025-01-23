package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import no.vebb.f1.util.PositionedCompetitor;
import no.vebb.f1.util.Table;

@Controller
@RequestMapping("/admin/season/{year}/manage")
public class ManageSeasonController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private Database db;

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

		List<String> header = Arrays.asList("Plass", "Sjåfør");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> startingGrid = db.getStartingGrid(raceId);
		for (PositionedCompetitor driver : startingGrid) {
			body.add(Arrays.asList(driver.position, driver.name));
		}

		return new Table("Starting grid", header, body);
	}

	private Table getRaceResultTable(int raceId) {
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> raceResult = db.getRaceResult(raceId);
		for (PositionedCompetitor driver : raceResult) {
			body.add(Arrays.asList(driver.position, driver.name, driver.points));
		}
		return new Table("Race result", header, body);
	}

	private Table getDriverStandingsTable(int raceId) {
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> standings = db.getDriverStandings(raceId);
		for (PositionedCompetitor driver : standings) {
			body.add(Arrays.asList(driver.position, driver.name, driver.points));
		}

		return new Table("Driver standings", header, body);
	}

	private Table getConstructorStandingsTable(int raceId) {
		List<String> header = Arrays.asList("Plass", "Konstruktør", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<PositionedCompetitor> standings = db.getConstructorStandings(raceId);
		for (PositionedCompetitor constructor : standings) {
			body.add(Arrays.asList(constructor.position, constructor.name, constructor.points));
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
		List<Integer> races = db.getRacesFromSeason(year);
		db.removeRaceOrderFromSeason(year);
		int currentPos = 1;
		for (int id : races) {
			if (id == raceId) {
				continue;
			}
			if (currentPos == position) {
				db.insertRaceOrder(raceId, year, currentPos);
				currentPos++;
			}
			db.insertRaceOrder(id, year, currentPos);
			currentPos++;
		}
		if (currentPos == position) {
			db.insertRaceOrder(raceId, year, currentPos);
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
		db.deleteRace(raceId);

		List<Integer> races = db.getRacesFromSeason(year);
		db.removeRaceOrderFromSeason(year);
		int currentPos = 1;
		for (int id : races) {
			db.insertRaceOrder(id, year, currentPos);
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
