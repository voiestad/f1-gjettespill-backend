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
import no.vebb.f1.util.CutoffRace;
import no.vebb.f1.util.InvalidYearException;
import no.vebb.f1.util.NoAvailableRaceException;
import no.vebb.f1.util.Table;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.Year;

@Controller
@RequestMapping("/race-guess")
public class RaceGuess {

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	@GetMapping
	public String guessOverview(Model model) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			CutoffRace race = db.getLatestRaceForPlaceGuess(year);
			if (cutoff.isAbleToGuessRace(race.id)) {
				return "redirect:/";
			}
			String title = String.format("%d. %s %d", race.position, race.name, year);
			model.addAttribute("title", title);
			
			List<Table> tables = new ArrayList<>();

			String[] categories = {"FIRST", "TENTH"};
			List<String> header = Arrays.asList("Navn", "Tippet", "Startet");
			for (String category : categories) {
				List<List<String>> body = db.getUserGuessesDriverPlace(race.id, category).stream()
					.map(userGuess -> Arrays.asList(userGuess.user, userGuess.driver, userGuess.position))
					.toList();
				String name = db.translateCategory(category);
				Table table = new Table(name, header, body);
				tables.add(table);
			}

			model.addAttribute("tables", tables);
			return "raceGuess";
		} catch (InvalidYearException e) {
			return "redirect:/";
		} catch (EmptyResultDataAccessException e) {
			return "redirect:/";
		} catch (NoAvailableRaceException e) {
			return "redirect:/";
		}
	}
}
