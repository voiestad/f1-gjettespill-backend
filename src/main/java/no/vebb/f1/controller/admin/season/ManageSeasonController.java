package no.vebb.f1.controller.admin.season;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.importing.Importer;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.StatsUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;

@Controller
@RequestMapping("/admin/season/{year}/manage")
public class ManageSeasonController {
	
	@Autowired
	private Database db;

	@Autowired
	private StatsUtil statsUtil;

	@GetMapping
	public String manageRacesInSeason(@RequestParam(value = "success", required = false) Boolean success,
			@PathVariable("year") int year, Model model) {
		Year seasonYear = new Year(year, db);
		if (success != null) {
			if (success) {
				model.addAttribute("successMessage", "Endringen ble lagret");
			} else {
				model.addAttribute("successMessage", "Endringen feilet");
			}
		}
		List<CutoffRace> races = db.getCutoffRaces(seasonYear);
		model.addAttribute("races", races);
		model.addAttribute("title", year);
		model.addAttribute("year", year);
		return "admin/manageSeason";
	}

	@GetMapping("/{raceId}")
	public String manageRacesInSeason(@PathVariable("raceId") int raceId, @PathVariable("year") int year, Model model) {
		Year seasonYear = new Year(year, db);
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
			if (!isRaceInSeason) {
				return "redirect:/admin/season/" + year + "/manage?success=false";
			}	
			
			List<Table> tables = new ArrayList<>();
			tables.add(statsUtil.getStartingGridTable(validRaceId));
			tables.add(statsUtil.getRaceResultTable(validRaceId));
			tables.add(statsUtil.getDriverStandingsTable(validRaceId));
			tables.add(statsUtil.getConstructorStandingsTable(validRaceId));
			tables.add(statsUtil.getFlagTable(validRaceId));
			
			model.addAttribute("tables", tables);
			model.addAttribute("title", year);
			return "util/tables";
		} catch (InvalidRaceException e) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
	}

	@PostMapping("/reload")
	@Transactional
	public String reloadRace(@RequestParam("id") int raceId, @PathVariable("year") int year) {
		Year seasonYear = new Year(year, db);
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
			if (!isRaceInSeason) {
				return "redirect:/admin/season/" + year + "/manage?success=false";
			}

			Importer importer = new Importer(db);
			importer.importRaceData(validRaceId);

			return "redirect:/admin/season/" + year + "/manage/" + raceId;
		} catch (InvalidRaceException e) {
			return "redirect:/admin/season/" + year + "/manage?success=false";

		}
	}

	@PostMapping("/move")
	@Transactional
	public String changeRaceOrder(@PathVariable int year, @RequestParam("id") int raceId,
			@RequestParam("newPosition") int position) {
		Year seasonYear = new Year(year, db);
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
			if (!isRaceInSeason) {
				return "redirect:/admin/season/" + year + "/manage?success=false";
			}
			int maxPos = db.getMaxRaceOrderPosition(seasonYear);
			boolean isPosOutOfBounds = position < 1 || position > maxPos;
			if (isPosOutOfBounds) {
				return "redirect:/admin/season/" + year + "/manage?success=false";
			}
			List<RaceId> races = db.getRacesFromSeason(new Year(year, db));
			db.removeRaceOrderFromSeason(seasonYear);
			int currentPos = 1;
			for (RaceId id : races) {
				if (id.equals(validRaceId)) {
					continue;
				}
				if (currentPos == position) {
					db.insertRaceOrder(validRaceId, year, currentPos);
					currentPos++;
				}
				db.insertRaceOrder(id, year, currentPos);
				currentPos++;
			}
			if (currentPos == position) {
				db.insertRaceOrder(validRaceId, year, currentPos);
			}
			Importer importer = new Importer(db);
			importer.importData();
			return "redirect:/admin/season/" + year + "/manage?success=true";
		} catch (InvalidRaceException e) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
	}

	@PostMapping("/delete")
	@Transactional
	public String deleteRace(@PathVariable int year, @RequestParam("id") int raceId) {
		Year seasonYear = new Year(year, db);
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
			if (!isRaceInSeason) {
				return "redirect:/admin/season/" + year + "/manage?success=false";
			}
			db.deleteRace(validRaceId);

			List<RaceId> races = db.getRacesFromSeason(seasonYear);
			db.removeRaceOrderFromSeason(seasonYear);
			int currentPos = 1;
			for (RaceId id : races) {
				db.insertRaceOrder(id, year, currentPos);
				currentPos++;
			}
			return "redirect:/admin/season/" + year + "/manage?success=true";
		} catch (InvalidRaceException e) {
			return "redirect:/admin/season/" + year + "/manage?success=false";
		}
	}

	@PostMapping("/add")
	@Transactional
	public String addRace(@PathVariable int year, @RequestParam("id") int raceId) {
		Year seasonYear = new Year(year, db);
		try {
			@SuppressWarnings("unused")
			RaceId validRaceId = new RaceId(raceId, db);
			return "redirect:/admin/season/" + year + "/manage?success=false";
		} catch (InvalidRaceException e) {
		}
		Importer importer = new Importer(db);
		importer.importRaceName(raceId, seasonYear);
		importer.importData();
		RaceId validRaceId = new RaceId(raceId, db);
		db.setCutoffRace(new Cutoff().getDefaultInstant(seasonYear), validRaceId);
		return "redirect:/admin/season/" + year + "/manage?success=true";
	}
}
