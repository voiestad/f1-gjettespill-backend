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
import no.vebb.f1.util.Table;

@Controller
@RequestMapping("/admin/season/{year}/points")
public class ManagePointsSystemController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private Database db;

	@GetMapping
	public String managePointsSystem(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		List<String> categories = db.getCategories();
		Map<String, String> categoryMap = new LinkedHashMap<>();
		for (String category : categories) {
			String translation = db.translateCategory(category);
			categoryMap.put(category, translation);
		}
		model.addAttribute("categories", categoryMap);

		List<Table> tables = ScoreController.getScoreMappingTables(year, db);
		model.addAttribute("tables", tables);

		model.addAttribute("title", year);
		model.addAttribute("year", year);
		return "managePointsSystem";
	}

	@PostMapping("/add")
	public String addPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		boolean isValidCategory = db.isValidCategory(category);
		if (!isValidCategory) {
			return "redirect:/admin/season/" + year + "/points";
		}

		int newDiff;
		try {
			newDiff = db.getMaxDiffInPointsMap(year, category) + 1;
		} catch (EmptyResultDataAccessException e) {
			newDiff = 0;
		}
		db.addDiffToPointsMap(category, newDiff, year);
		return "redirect:/admin/season/" + year + "/points";
	}

	@PostMapping("/delete")
	public String deletePointsMapping(@PathVariable("year") int year, @RequestParam("category") String category) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		boolean isValidCategory = db.isValidCategory(category);
		if (!isValidCategory) {
			return "redirect:/admin/season/" + year + "/points";
		}

		int maxDiff;
		try {
			maxDiff = db.getMaxDiffInPointsMap(year, category);
		} catch (EmptyResultDataAccessException e) {
			return "redirect:/admin/season/" + year + "/points";
		}

		db.removeDiffToPointsMap(category, maxDiff, year);
		return "redirect:/admin/season/" + year + "/points";
	}

	@PostMapping("/set")
	public String setPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category,
			@RequestParam("diff") int diff, @RequestParam("points") int points) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		boolean isValidCategory = db.isValidCategory(category);
		if (!isValidCategory) {
			return "redirect:/admin/season/" + year + "/points";
		}

		boolean isValidDiff = db.isValidDiffInPointsMap(category, diff, year);
		if (!isValidDiff) {
			return "redirect:/admin/season/" + year + "/points";
		}

		boolean isValidPoints = points >= 0;
		if (!isValidPoints) {
			return "redirect:/admin/season/" + year + "/points";
		}

		db.setNewDiffToPointsInPointsMap(category, diff, year, points);
		return "redirect:/admin/season/" + year + "/points";
	}
}
