package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.StatsUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/stats")
public class StatsController {

	@Autowired
	private Database db;

	@Autowired
	private StatsUtil statsUtil;

	@GetMapping
	public String chooseYear(Model model) {
		Map<String, String> linkMap = new LinkedHashMap<>();
		model.addAttribute("linkMap", linkMap);
		model.addAttribute("title", "Velg år");
		List<Year> years = db.getAllValidYears();
		for (Year year : years) {
			linkMap.put(String.valueOf(year), "/stats/" + year);
		}
		return "util/linkList";
	}

	@GetMapping("/{year}")
	public String chooseRace(@PathVariable("year") int year, Model model) {
		try {
			Map<String, String> linkMap = new LinkedHashMap<>();
			model.addAttribute("linkMap", linkMap);
			model.addAttribute("title", "Velg løp");
			Year seasonYear = new Year(year, db);
			List<CutoffRace> races = db.getCutoffRaces(seasonYear);
			for (CutoffRace race : races) {
				linkMap.put(race.position + ". " + race.name, "/stats/" + year + "/" + race.id);
			}
			return "util/linkList";
		} catch (InvalidYearException e) {
			return "redirect:/stats";	
		}
	}

	@GetMapping("/{year}/{raceId}")
	public String manageRacesInSeason(@PathVariable("raceId") int raceId, @PathVariable("year") int year, Model model) {
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			
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
			return "redirect:/stats/" + year;
		}
	}
}
