package no.vebb.f1.controller.admin.season;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.controller.ScoreController;
import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Category;
import no.vebb.f1.util.InvalidCategoryException;
import no.vebb.f1.util.Table;
import no.vebb.f1.util.Year;

@Controller
@RequestMapping("/admin/season/{year}/points")
public class ManagePointsSystemController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private Database db;

	@GetMapping
	public String managePointsSystem(@PathVariable("year") int year, Model model) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);

		List<Category> categories = db.getCategories();
		Map<String, String> categoryMap = new LinkedHashMap<>();
		for (Category category : categories) {
			String translation = db.translateCategory(category);
			categoryMap.put(category.value, translation);
		}
		model.addAttribute("categories", categoryMap);

		List<Table> tables = ScoreController.getScoreMappingTables(seasonYear, db);
		model.addAttribute("tables", tables);

		model.addAttribute("title", year);
		model.addAttribute("year", year);
		return "managePointsSystem";
	}

	@PostMapping("/add")
	public String addPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		int newDiff;
		try {
			Category validCategory = new Category(category, db);
			try {
			newDiff = db.getMaxDiffInPointsMap(seasonYear, validCategory) + 1;
			} catch (EmptyResultDataAccessException e) {
			newDiff = 0;
			} 
			db.addDiffToPointsMap(validCategory, newDiff, seasonYear);
		} catch (InvalidCategoryException e) {
			return "redirect:/admin/season/" + year + "/points";
		}
		return "redirect:/admin/season/" + year + "/points";
	}

	@PostMapping("/delete")
	public String deletePointsMapping(@PathVariable("year") int year, @RequestParam("category") String category) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		try {
			Category validCategory = new Category(category, db);
			int maxDiff = db.getMaxDiffInPointsMap(seasonYear, validCategory);
			db.removeDiffToPointsMap(validCategory, maxDiff, seasonYear);
		} catch (EmptyResultDataAccessException e) {
		} catch (InvalidCategoryException e) {
		}
		return "redirect:/admin/season/" + year + "/points";
	}

	@PostMapping("/set")
	public String setPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category,
			@RequestParam("diff") int diff, @RequestParam("points") int points) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		try {
			Category validCategory = new Category(category, db);
			boolean isValidDiff = db.isValidDiffInPointsMap(validCategory, diff, seasonYear);
			if (!isValidDiff) {
				return "redirect:/admin/season/" + year + "/points";
			}

			boolean isValidPoints = points >= 0;
			if (!isValidPoints) {
				return "redirect:/admin/season/" + year + "/points";
			}

			db.setNewDiffToPointsInPointsMap(validCategory, diff, seasonYear, points);
		} catch (EmptyResultDataAccessException e) {
		} catch (InvalidCategoryException e) {
		}
		return "redirect:/admin/season/" + year + "/points";
	}
}
