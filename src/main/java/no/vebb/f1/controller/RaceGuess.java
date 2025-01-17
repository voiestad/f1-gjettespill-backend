package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.NoAvailableRaceException;
import no.vebb.f1.util.Table;
import no.vebb.f1.util.TimeUtil;

@Controller
@RequestMapping("/race-guess")
public class RaceGuess {

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	@GetMapping()
	public String guessOverview(Model model) {
		int year = TimeUtil.getCurrentYear();
		try {
			Map<String, Object> res = db.getLatestRaceForPlaceGuess(year);
			int raceId = (int) res.get("id");
			if (cutoff.isAbleToGuessRace(raceId)) {
				return "redirect:/";
			}
			int raceNumberSeason = (int) res.get("position");
			String raceName = (String) res.get("name");
			String title = String.format("%d. %s %d", raceNumberSeason, raceName, year);
			model.addAttribute("title", title);
			
			List<Table> tables = new ArrayList<>();

			String[] categories = {"FIRST", "TENTH"};
			List<String> header = Arrays.asList("Navn", "Tippet", "Startet");
			for (String category : categories) {
				List<Map<String, Object>> sqlRes = db.getUserGuessesDriverPlace(raceId, category);
				List<List<String>> body = new ArrayList<>();
				for (Map<String, Object> row : sqlRes) {
					String username = (String) row.get("username");
					String driver = (String) row.get("driver");
					int position = (int) row.get("position");
					body.add(Arrays.asList(username, driver, String.valueOf(position)));
				}
				String name = db.translateCategory(category);
				Table table = new Table(name, header, body);
				tables.add(table);
			}

			
			model.addAttribute("tables", tables);
			return "raceGuess";
		} catch (EmptyResultDataAccessException e) {
			return "redirect:/";
		} catch (NoAvailableRaceException e) {
			return "redirect:/";
		}
	}
}
