package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.util.ArrayList;
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
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/admin/season")
public class SeasonController {
	
	@Autowired
	private Database db;

	@GetMapping
	public String seasonAdminOverview(Model model) {
		model.addAttribute("title", "Administrer sesonger");
		Map<String, String> linkMap = new LinkedHashMap<>();
		List<Year> years = db.getAllValidYears();
		for (Year year : years) {
			linkMap.put(String.valueOf(year), "/admin/season/" + year);
		}
		linkMap.put("Legg til ny sesong", "/admin/season/add");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/{year}")
	public String seasonMenu(@RequestParam(value = "success", required = false) Boolean success,
			@PathVariable("year") int year, Model model) {
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
		return "addSeason";
	}

	@PostMapping("/add")
	public String addSeason(@RequestParam("year") int year, @RequestParam("start") int start,
			@RequestParam("end") int end, Model model) {
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
		Instant time = new Cutoff().getDefaultInstant(seasonYear);
		db.setCutoffYear(time, seasonYear);
		setDefaultCutoffRaces(seasonYear, time);

		return "redirect:/admin/season";
	}

	private void setDefaultCutoffRaces(Year year, Instant time) {
		List<RaceId> races = db.getRacesFromSeason(year);
		for (RaceId id : races) {
			db.setCutoffRace(time, id);
		}
	}
}
