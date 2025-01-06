package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.Table;

@Controller
@RequestMapping("/race-guess")
public class RaceGuess {
	
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Cutoff cutoff;

	public RaceGuess(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping()
	public String guessOverview(Model model) {
		final String getRaceIdSql = """
			SELECT ro.id AS id, ro.position AS position, r.name AS name
			FROM RaceOrder ro
			JOIN StartingGrid sg ON ro.id = sg.race_number
			JOIN Race r ON r.id = ro.id
			WHERE ro.year = ?
			ORDER BY ro.position DESC
			LIMIT 1;
		""";
		int year = cutoff.getCurrentYear();
		try {
			Map<String, Object> res = jdbcTemplate.queryForMap(getRaceIdSql, year);
			int raceId = (int) res.get("id");
			if (cutoff.isAbleToGuessRace(raceId)) {
				return "redirect:/";
			}
			int raceNumberSeason = (int) res.get("position");
			String raceName = (String) res.get("name");
			String title = String.format("%d. %s %d", raceNumberSeason, raceName, year);
			model.addAttribute("title", title);
			
			List<Table> tables = new ArrayList<>();
			final String getGuessSql = """
				SELECT u.username AS username, dpg.driver AS driver, sg.position AS position
				FROM DriverPlaceGuess dpg
				JOIN User u ON u.id = dpg.guesser
				JOIN StartingGrid sg ON sg.race_number = dpg.race_number AND sg.driver = dpg.driver
				WHERE dpg.race_number = ? AND dpg.category = ?
				ORDER BY u.username ASC
			""";

			String[] categories = {"FIRST", "TENTH"};
			List<String> header = Arrays.asList("Navn", "Tippet", "Startet");
			for (String category : categories) {
				List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getGuessSql, raceId, category);
				List<List<String>> body = new ArrayList<>();
				for (Map<String, Object> row : sqlRes) {
					String username = (String) row.get("username");
					String driver = (String) row.get("driver");
					int position = (int) row.get("position");
					body.add(Arrays.asList(username, driver, String.valueOf(position)));
				}
				String name = translateCategory(category);
				Table table = new Table(name, header, body);
				tables.add(table);
			}

			
			model.addAttribute("tables", tables);
			return "raceGuess";
		} catch (EmptyResultDataAccessException e) {
			return "redirect:/";
		}
		
	}

	private String translateCategory(String category) {
		final String translationSql = """
				SELECT translation
				FROM CategoryTranslation
				WHERE category = ?
				""";

		return jdbcTemplate.queryForObject(translationSql, String.class, category);
	}
}
