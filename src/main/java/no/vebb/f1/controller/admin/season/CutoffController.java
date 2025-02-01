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
import no.vebb.f1.util.Year;

/**
 * CutoffController is responsible for changing cutoff for races and season.
 */
@Controller
@RequestMapping("/admin/season/{year}/cutoff")
public class CutoffController {

	@Autowired
	private UserService userService;

	@Autowired
	private Database db;

	/**
	 * Handles GET requests for managing cutoff for the given season. Gives a list
	 * of cutoffs for each race of the season and the cutoff for the season. Will
	 * redirect to / if user is not admin and /admin/season if year is invalid.
	 * 
	 * @param year  of season
	 * @param model
	 * @return cutoff template
	 */
	@GetMapping
	public String manageCutoff(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		Year seasonYear = new Year(year, db);

		List<CutoffRace> races = db.getCutoffRaces(seasonYear);
		LocalDateTime cutoffYear = db.getCutoffYearLocalTime(seasonYear);

		model.addAttribute("title", year);
		model.addAttribute("races", races);
		model.addAttribute("cutoffYear", cutoffYear);
		return "cutoff";
	}

	/**
	 * Handels POST mapping for setting the cutoff for a race. Will
	 * redirect to / if user is not admin and /admin/season if year is invalid. If
	 * race ID is or time has invalid format, nothing will change in the database.
	 * 
	 * @param year   of season
	 * @param raceId of race
	 * @param cutoff to set for the race in local time
	 * @return redirect to origin if user is admin and season valid
	 */
	@PostMapping("/setRace")
	public String setCutoffRace(@PathVariable("year") int year, @RequestParam("id") int raceId,
			@RequestParam("cutoff") String cutoff) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		Year seasonYear = new Year(year, db);
		boolean isValidRaceId = db.isValidRaceInSeason(raceId, seasonYear);
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/cutoff";
		}
		try {
			Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
			db.setCutoffRace(cutoffTime, raceId);
		} catch (DateTimeParseException e) {

		}
		return "redirect:/admin/season/" + year + "/cutoff";
	}

	/**
	 * Handels POST mapping for setting the cutoff for a season. Will
	 * redirect to / if user is not admin and /admin/season if year is invalid. If
	 * time has invalid format, nothing will change in the database.
	 * 
	 * @param year   of season
	 * @param raceId of race
	 * @param cutoff to set for the race in local time
	 * @return redirect to origin if user is admin and season valid
	 */
	@PostMapping("/setYear")
	public String setCutoffYear(@PathVariable("year") int year, @RequestParam("cutoff") String cutoff) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		Year seasonYear = new Year(year, db);
		try {
			Instant cutoffTime = TimeUtil.parseTimeInput(cutoff);
			db.setCutoffYear(cutoffTime, seasonYear);
		} catch (DateTimeParseException e) {

		}
		return "redirect:/admin/season/" + year + "/cutoff";
	}
}
