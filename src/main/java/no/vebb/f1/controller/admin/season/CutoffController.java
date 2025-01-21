package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.CutoffRace;
import no.vebb.f1.util.TimeUtil;

@Controller
@RequestMapping("/admin/season/{year}/cutoff")
public class CutoffController {

	@Autowired
	private UserService userService;

	@Autowired
	private Database db;

	@GetMapping
	public String manageCutoff(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		List<CutoffRace> races = db.getCutoffRaces(year);
		LocalDateTime cutoffYear = db.getCutoffYearLocalTime(year);
		
		model.addAttribute("title", year);
		model.addAttribute("races", races);
		model.addAttribute("cutoffYear", cutoffYear);
		return "cutoff";
	}

	@PostMapping("/setRace")
	public String setCutoffRace(@PathVariable("year") int year, @RequestParam("id") int id,
			@RequestParam("cutoff") String cutoff) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean isValidRaceId = db.isValidRaceInSeason(id, year);
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/cutoff";
		}
		try {
			Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
			db.setCutoffRace(cutoffTime, id);
		} catch (DateTimeParseException e) {

		}
		return "redirect:/admin/season/" + year + "/cutoff";
	}

	@PostMapping("/setYear")
	public String setCutoffYear(@PathVariable("year") int year, @RequestParam("cutoff") String cutoff) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		try {
			Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
			db.setCutoffYear(cutoffTime, year);
		} catch (DateTimeParseException e) {

		}
		return "redirect:/admin/season/" + year + "/cutoff";
	}
}
