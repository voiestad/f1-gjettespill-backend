package no.vebb.f1.controller;

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
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Controller
@RequestMapping("/score")
public class ScoreController {

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping
	public String scoreMappingTables(Model model) {
		List<Table> scoreMappingTables;
		try {
			scoreMappingTables = getScoreMappingTables(new Year(TimeUtil.getCurrentYear(), db), db);
		} catch (InvalidYearException e) {
			scoreMappingTables = Arrays.asList();
		}
	model.addAttribute("tables", scoreMappingTables);
		model.addAttribute("title", "Poengberegning");
		model.addAttribute("loggedOut", !userService.isLoggedIn());
		return "tables";
	}

	public static List<Table> getScoreMappingTables(Year year, Database db) {
		List<Category> categories = db.getCategories();
		return categories.stream()
			.map(category -> getTable(category, year, db))
			.toList();
	}

	private static Table getTable(Category category, Year year, Database db) {
		List<String> header = Arrays.asList("Differanse", "Poeng");
		Map<Integer, Points> map = db.getDiffPointsMap(year, category);
		List<List<String>> body = map.entrySet().stream()
			.map(entry -> Arrays.asList(
				String.valueOf(entry.getKey()), 
				String.valueOf(entry.getValue())))
			.toList();
		String translation = db.translateCategory(category);
		return new Table(translation, header, body);
	}

}
