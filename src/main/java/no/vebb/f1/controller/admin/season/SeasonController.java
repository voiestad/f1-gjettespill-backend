package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import no.vebb.f1.util.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/admin/season")
public class SeasonController {
	
	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping
	public String seasonAdminOverview(Model model) {
		userService.adminCheck();
		model.addAttribute("title", "Administrer sesonger");
		Map<String, String> linkMap = new LinkedHashMap<>();
		List<Integer> years = db.getAllValidYears();
		for (Integer year : years) {
			linkMap.put(String.valueOf(year), "/admin/season/" + year);
		}
		linkMap.put("Legg til ny sesong", "/admin/season/add");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/{year}")
	public String seasonMenu(@RequestParam(value = "success", required = false) Boolean success,
			@PathVariable("year") int year, Model model) {
		userService.adminCheck();
		new Year(year, db);
		model.addAttribute("title", year);
		Map<String, String> linkMap = new LinkedHashMap<>();
		String basePath = "/admin/season/" + year;
		linkMap.put("Endring av løp", basePath + "/manage");
		linkMap.put("Frister", basePath + "/cutoff");
		linkMap.put("F1 Deltakere", basePath + "/competitors");
		linkMap.put("Poengsystem", basePath + "/points");

		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/add")
	public String addSeasonForm() {
		userService.adminCheck();
		return "addSeason";
	}

	@PostMapping("/add")
	public String addSeason(@RequestParam("year") int year, @RequestParam("start") int start,
			@RequestParam("end") int end, Model model) {
		userService.adminCheck();
		try {
			new Year(year, db);
			model.addAttribute("error", String.format("Sesongen %d er allerede lagt til", year));
			return "addSeason";
		} catch (InvalidYearException e) {
		}
		if (start > end) {
			model.addAttribute("error", "Starten av året kan ikke være etter slutten av året");
			return "addSeason";
		}

		List<Integer> races = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			races.add(i);
		}

		Importer importer = new Importer(db);
		importer.importRaceNames(races, year);
		importer.importData();

		Year seasonYear = new Year(year, db);
		setDefaultCutoffYear(seasonYear);
		setDefaultCutoffRaces(seasonYear);

		return "redirect:/admin/season";
	}

	private Instant getDefaultInstant(Year year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year.value);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.toInstant();
	}

	private void setDefaultCutoffRaces(Year year) {
		Instant time = getDefaultInstant(year);
		List<Integer> races = db.getRacesFromSeason(year);
		for (int id : races) {
			db.setCutoffRace(time, id);
		}
	}

	private void setDefaultCutoffYear(Year year) {
		Instant time = getDefaultInstant(year);
		db.setCutoffYear(time, year);
	}
}
