package no.vebb.f1.controller.admin;

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
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidFlagException;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/admin/flag")
public class FlagController {

	@Autowired
	private UserService userService;

	@Autowired
	private Database db;

	@GetMapping
	public String flagChooseYear(Model model) {
		userService.adminCheck();
		model.addAttribute("title", "Velg år");
		Map<String, String> linkMap = new LinkedHashMap<>();
		List<Year> years = db.getAllValidYears();
		for (Year year : years) {
			linkMap.put(String.valueOf(year), "/admin/flag/" + year);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/{year}")
	public String flagChooseRace(@PathVariable("year") int year, Model model) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		model.addAttribute("title", "Velg løp");
		Map<String, String> linkMap = new LinkedHashMap<>();
		List<CutoffRace> races = db.getCutoffRaces(seasonYear);
		for (CutoffRace race : races) {
			linkMap.put(race.position + ". " + race.name, "/admin/flag/" + year + "/" + race.id);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/{year}/{id}")
	public String registerFlags(@PathVariable("year") int year, @PathVariable("id") int raceId, Model model) {
		userService.adminCheck();
		try {
			Year seasonYear = new Year(year, db);
			RaceId validRaceId = new RaceId(raceId, db);
			boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
			if (!isRaceInSeason) {
				return "redirect:/admin/flag";
			}
			List<String> flags = db.getFlags();
			model.addAttribute("flags", flags);
			model.addAttribute("raceId", raceId);
			
			List<RegisteredFlag> registeredFlags = db.getRegisteredFlags(validRaceId);
			model.addAttribute("registeredFlags", registeredFlags);
			model.addAttribute("title", "Flagg " + db.getRaceName(validRaceId));
		} catch (InvalidRaceException e) {
		} catch (InvalidYearException e) {
		}
		return "noteFlags";
	}

	@PostMapping("/add")
	public String registerFlag(@RequestParam("flag") String flag, @RequestParam("round") int round,
			@RequestParam("raceId") int raceId, @RequestParam("origin") String origin) {
		userService.adminCheck();
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			Flag validFlag = new Flag(flag, db);
			if (round < 1 || round > 100) {
				throw new IllegalArgumentException("Round : '" + round + "' out of bounds. Range: 1-100.");
			}
			db.insertFlagStats(validFlag, round, validRaceId);
		} catch (InvalidRaceException e) {
		} catch (InvalidFlagException e) {
		} catch (IllegalArgumentException e) {
		}
		return "redirect:" + origin;
	}

	@PostMapping("/delete")
	public String deleteFlag(@RequestParam("id") int id, @RequestParam("origin") String origin) {
		userService.adminCheck();

		db.deleteFlagStatsById(id);

		return "redirect:" + origin;
	}

}
