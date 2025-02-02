package no.vebb.f1.controller.admin;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import no.vebb.f1.util.CutoffRace;
import no.vebb.f1.util.RegisteredFlag;
import no.vebb.f1.util.Year;

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
		List<Integer> years = db.getAllValidYears();
		for (Integer year : years) {
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
	public String registrerFlags(@PathVariable("year") int year, @PathVariable("id") int raceId, Model model) {
		userService.adminCheck();
		List<String> flags = db.getFlags();
		model.addAttribute("flags", flags);
		model.addAttribute("raceId", raceId);

		List<RegisteredFlag> registeredFlags = db.getRegisteredFlags(raceId);
		model.addAttribute("registeredFlags", registeredFlags);
		model.addAttribute("title", "Flagg " + db.getRaceName(raceId));
		return "noteFlags";
	}

	@PostMapping("/add")
	public String registerFlag(@RequestParam("flag") String flag, @RequestParam("round") int round,
			@RequestParam("raceId") int raceId, @RequestParam("origin") String origin) {
		userService.adminCheck();
		Set<String> flags = new HashSet<>(db.getFlags());
		if (!flags.contains(flag)) {
			return "redirect:" + origin;
		}

		db.insertFlagStats(flag, round, raceId);

		return "redirect:" + origin;
	}

	@PostMapping("/delete")
	public String deleteFlag(@RequestParam("id") int id, @RequestParam("origin") String origin) {
		userService.adminCheck();

		db.deleteFlagStatsById(id);

		return "redirect:" + origin;
	}

}
