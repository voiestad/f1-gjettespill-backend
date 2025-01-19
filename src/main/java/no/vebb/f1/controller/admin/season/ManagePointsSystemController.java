package no.vebb.f1.controller.admin.season;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
	
	private JdbcTemplate jdbcTemplate;

	public ManagePointsSystemController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public String managePointsSystem(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		final String getCategories = "SELECT name FROM Category";
		List<String> categories = jdbcTemplate.queryForList(getCategories, String.class);
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

		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		final String validateCategory = "SELECT COUNT(*) FROM Category WHERE name = ?";
		boolean isValidCategory = jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
		if (!isValidCategory) {
			return "redirect:/admin/season/" + year + "/points";
		}

		final String getMaxDiff = "SELECT MAX(diff) FROM DiffPointsMap WHERE year = ? AND category = ?";
		int newDiff;
		try {
			newDiff = jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year, category) + 1;
		} catch (EmptyResultDataAccessException e) {
			newDiff = 0;
		}

		final String addDiff = "INSERT INTO DiffPointsMap (category, diff, points, year) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(addDiff, category, newDiff, 0, year);
		return "redirect:/admin/season/" + year + "/points";
	}

	@PostMapping("/delete")
	public String deletePointsMapping(@PathVariable("year") int year, @RequestParam("category") String category) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		final String validateCategory = "SELECT COUNT(*) FROM Category WHERE name = ?";
		boolean isValidCategory = jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
		if (!isValidCategory) {
			return "redirect:/admin/season/" + year + "/points";
		}

		final String getMaxDiff = "SELECT MAX(diff) FROM DiffPointsMap WHERE year = ? AND category = ?";
		int maxDiff;
		try {
			maxDiff = jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year, category);
		} catch (EmptyResultDataAccessException e) {
			return "redirect:/admin/season/" + year + "/points";
		}

		final String deleteRowWithDiff = "DELETE FROM DiffPointsMap WHERE year = ? AND category = ? AND diff = ?";
		jdbcTemplate.update(deleteRowWithDiff, year, category, maxDiff);
		return "redirect:/admin/season/" + year + "/points";
	}

	@PostMapping("/set")
	public String setPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category,
			@RequestParam("diff") int diff, @RequestParam("points") int points) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		final String validateCategory = "SELECT COUNT(*) FROM Category WHERE name = ?";
		boolean isValidCategory = jdbcTemplate.queryForObject(validateCategory, Integer.class, category) > 0;
		if (!isValidCategory) {
			return "redirect:/admin/season/" + year + "/points";
		}

		final String validateDiff = "SELECT COUNT(*) FROM DiffPointsMap WHERE year = ? AND category = ? AND diff = ?";
		boolean isValidDiff = jdbcTemplate.queryForObject(validateDiff, Integer.class, year, category, diff) > 0;
		if (!isValidDiff) {
			return "redirect:/admin/season/" + year + "/points";
		}

		boolean isValidPoints = points >= 0;
		if (!isValidPoints) {
			return "redirect:/admin/season/" + year + "/points";
		}

		final String setNewPoints = """
				UPDATE DiffPointsMap
				SET points = ?
				WHERE diff = ? AND year = ? AND category = ?
				""";
		jdbcTemplate.update(setNewPoints, points, diff, year, category);
		return "redirect:/admin/season/" + year + "/points";
	}
}
