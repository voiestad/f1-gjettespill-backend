package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Table;
import no.vebb.f1.util.TimeUtil;

@Controller
@RequestMapping("/score")
public class ScoreController {

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping
	public String scoreMappingTables(Model model) {
		List<Table> scoreMappingTables = getScoreMappingTables(TimeUtil.getCurrentYear(), db);
		model.addAttribute("tables", scoreMappingTables);
		model.addAttribute("title", "Poengberegning");
		model.addAttribute("loggedOut", !userService.isLoggedIn());
		return "tables";
	}

	public static List<Table> getScoreMappingTables(int year, Database db) {
		List<Table> scoreMappingTables = new ArrayList<>();
		List<String> categories = db.getCategories();
		for (String category : categories) {
			scoreMappingTables.add(getTable(category, year, db));
		}

		return scoreMappingTables;
	}

	private static Table getTable(String category, int year, Database db) {
		List<String> header = Arrays.asList("Differanse", "Poeng");
		List<List<String>> body = new ArrayList<>();
		List<Map<String, Object>> rows = db.getPointsDiffMap(year, category);
		for (Map<String, Object> row : rows) {
			int diff = (int) row.get("diff");
			int points = (int) row.get("points");
			body.add(Arrays.asList(String.valueOf(diff), String.valueOf(points)));
		}
		String translation = db.translateCategory(category);
		return new Table(translation, header, body);
	}

}
