package no.vebb.f1.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		List<String> categories = db.getCategories();
		return categories.stream()
			.map(category -> getTable(category, year, db))
			.collect(Collectors.toList());
	}

	private static Table getTable(String category, int year, Database db) {
		List<String> header = Arrays.asList("Differanse", "Poeng");
		List<Map<String, Object>> rows = db.getPointsDiffMap(year, category);
		List<List<String>> body = rows.stream()
			.map(row -> Arrays.asList(
				String.valueOf((int) row.get("diff")), 
				String.valueOf((int) row.get("points"))))
			.collect(Collectors.toList());
		String translation = db.translateCategory(category);
		return new Table(translation, header, body);
	}

}
