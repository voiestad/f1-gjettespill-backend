package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;

/**
 * Class is responsible for displaying guesses for the users for races.
 */
@Controller
@RequestMapping("/race-guess")
public class RaceGuessController {

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	/**
	 * Handles GET requests for /race-guess. If there is the guesses are not
	 * available, redirects to /. A race is only available when the cutoff of the
	 * latest starting grid of the current year has passed.
	 */
	@GetMapping
	public String guessOverview(Model model) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			CutoffRace race = db.getLatestRaceForPlaceGuess(year);
			if (cutoff.isAbleToGuessRace(race.id)) {
				return "redirect:/";
			}
			String title = String.format("%d. %s %d", race.position, race.name, year.value);
			model.addAttribute("title", title);

			List<Table> tables = new ArrayList<>();

			Category[] categories = { new Category("FIRST", db), new Category("TENTH", db) };
			List<String> header = Arrays.asList("Navn", "Tippet", "Startet");
			for (Category category : categories) {
				List<List<String>> body = db.getUserGuessesDriverPlace(race.id, category).stream()
						.map(userGuess -> Arrays.asList(userGuess.user, userGuess.driver, userGuess.position))
						.toList();
				String name = db.translateCategory(category);
				Table table = new Table(name, header, body);
				tables.add(table);
			}

			model.addAttribute("tables", tables);
			return "util/tables";
		} catch (InvalidYearException e) {
			return "redirect:/";
		} catch (EmptyResultDataAccessException e) {
			return "redirect:/";
		} catch (NoAvailableRaceException e) {
			return "redirect:/";
		}
	}
}
