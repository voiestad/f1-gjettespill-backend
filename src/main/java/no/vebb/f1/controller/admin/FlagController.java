package no.vebb.f1.controller.admin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.SessionType;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidFlagException;
import no.vebb.f1.util.exception.InvalidRaceException;
import no.vebb.f1.util.exception.InvalidSessionTypeException;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/admin/flag")
public class FlagController {

	@Autowired
	private Database db;

	@GetMapping
	public String flagChooseYear(Model model) {
		
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
		try {
			Year seasonYear = new Year(year, db);
			model.addAttribute("title", "Velg løp");
			Map<String, String> linkMap = new LinkedHashMap<>();
			List<CutoffRace> races = db.getCutoffRaces(seasonYear);
			for (CutoffRace race : races) {
				linkMap.put(race.position + ". " + race.name, "/admin/flag/" + year + "/" + race.id);
			}
			model.addAttribute("linkMap", linkMap);
			return "linkList";
		} catch (InvalidYearException e) {
			return "redirect:/admin/flag";
		}
	}

	@GetMapping("/{year}/{id}")
	public String registerFlags(@PathVariable("year") int year, @PathVariable("id") int raceId, 
		@RequestParam(value = "session", required = false) String selectedSessionType,
		@RequestParam(value = "flag", required = false) String selectedFlag,
		@RequestParam(value = "round", required = false) Integer selectedRound,
		Model model) {
		try {
			Year seasonYear = new Year(year, db);
			RaceId validRaceId = new RaceId(raceId, db);
			boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
			if (!isRaceInSeason) {
				return "redirect:/admin/flag/" + year;
			}
			List<Flag> flags = db.getFlags();
			model.addAttribute("flags", flags);
			List<SessionType> sessionTypes = db.getSessionTypes();
			model.addAttribute("sessionTypes", sessionTypes);
			model.addAttribute("raceId", raceId);
			List<RegisteredFlag> registeredFlags = db.getRegisteredFlags(validRaceId);
			model.addAttribute("registeredFlags", registeredFlags);
			model.addAttribute("title", "Flagg " + db.getRaceName(validRaceId));

			SessionType sessionType = null;
			try {
				if (selectedSessionType != null) {
					sessionType = new SessionType(selectedSessionType, db);
				}
			} catch (InvalidSessionTypeException e) {
			}
			model.addAttribute("selectedSessionType", sessionType);
			
			Flag flag = null;
			try {
				if (selectedFlag != null) {
					flag = new Flag(selectedFlag, db);
				}
			} catch (InvalidFlagException e) {
			}
			model.addAttribute("selectedFlag", flag);
			
			int round = 1;
			if (selectedRound != null && isValidRound(selectedRound)) {
				round = selectedRound;
			}
			model.addAttribute("selectedRound", round);

			return "noteFlags";
		} catch (InvalidRaceException e) {
			return "redirect:/admin/flag/" + year;
		} catch (InvalidYearException e) {
			return "redirect:/admin/flag";
		}
	}

	@PostMapping("/add")
	@Transactional
	public String registerFlag(@RequestParam("flag") String flag, @RequestParam("round") int round,
			@RequestParam("raceId") int raceId, @RequestParam("sessionType") String sessionType,
			@RequestParam("origin") String origin) {
		try {
			RaceId validRaceId = new RaceId(raceId, db);
			Flag validFlag = new Flag(flag, db);
			SessionType validSessionType = new SessionType(sessionType, db);
			if (!isValidRound(round)) {
				throw new IllegalArgumentException("Round : '" + round + "' out of bounds. Range: 1-100.");
			}
			db.insertFlagStats(validFlag, round, validRaceId, validSessionType);
			String originWithoutParameters = removeParameters(origin);
			String originWithParameters = String.format(
				"%s?session=%s&flag=%s&round=%d",
				originWithoutParameters,
				validSessionType,
				validFlag,
				round);
			return "redirect:" + originWithParameters;
		} catch (InvalidRaceException e) {
		} catch (InvalidFlagException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvalidSessionTypeException e) {
		}

		return "redirect:" + origin;
	}

	@PostMapping("/delete")
	@Transactional
	public String deleteFlag(@RequestParam("id") int id, @RequestParam("origin") String origin) {
		db.deleteFlagStatsById(id);
		return "redirect:" + origin;
	}

	private boolean isValidRound(int round) {
		return round >= 1 && round <= 100;
	}

	private String removeParameters(String path) {
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '?') {
				return path.substring(0, i);
			}
		}

		return path;
	}

}
