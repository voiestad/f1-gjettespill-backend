package no.vebb.f1.controller.admin.season;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidCategoryException;
import no.vebb.f1.util.exception.InvalidDiffException;
import no.vebb.f1.util.exception.InvalidPointsException;

/**
 * Class is responsible for changing the points system for a specified year.
 */
@Controller
@RequestMapping("/admin/season/{year}/points")
public class ManagePointsSystemController {
	
	@Autowired
	private Database db;

	/**
	 * Handles GET requests to /admin/season/{year}/points.
	 */
	@GetMapping
	public String managePointsSystem(@PathVariable("year") int year, Model model,
			@RequestParam(value = "category", required = false) String selectedCategory,
			@RequestParam(value = "diff", required = false, defaultValue = "0") int diff,
			@RequestParam(value = "points", required = false, defaultValue = "0") int points) {
		new Year(year, db);

		// Return available categories
		return "admin/managePointsSystem";
	}

	@PostMapping("/add")
	@Transactional
	public String addPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category,
			@RequestParam(value = "diff", required = false, defaultValue = "0") int diff,
			@RequestParam(value = "points", required = false, defaultValue = "0") int points) {
		Year seasonYear = new Year(year, db);
		Diff newDiff;
		try {
			Category validCategory = new Category(category, db);
			try {
				newDiff = db.getMaxDiffInPointsMap(seasonYear, validCategory).add(new Diff(1));
			} catch (NullPointerException e) {
				newDiff = new Diff();;
			} 
			db.addDiffToPointsMap(validCategory, newDiff, seasonYear);
		} catch (InvalidCategoryException e) {
			return "redirect:/admin/season/" + year + "/points";
		}
		return String.format("redirect:/admin/season/%d/points?category=%s&diff=%d&points=%d",
			year, category, diff, points);
	}

	@PostMapping("/delete")
	@Transactional
	public String deletePointsMapping(@PathVariable("year") int year, @RequestParam("category") String category,
			@RequestParam(value = "diff", required = false, defaultValue = "0") int diff,
			@RequestParam(value = "points", required = false, defaultValue = "0") int points) {
		Year seasonYear = new Year(year, db);
		try {
			Category validCategory = new Category(category, db);
			Diff maxDiff = db.getMaxDiffInPointsMap(seasonYear, validCategory);
			db.removeDiffToPointsMap(validCategory, maxDiff, seasonYear);
		} catch (NullPointerException e) {
		} catch (InvalidCategoryException e) {
		}
		return String.format("redirect:/admin/season/%d/points?category=%s&diff=%d&points=%d",
			year, category, diff, points);
	}

	@PostMapping("/set")
	@Transactional
	public String setPointsMapping(@PathVariable("year") int year, @RequestParam("category") String category,
			@RequestParam("diff") int diff, @RequestParam("points") int points) {
		Year seasonYear = new Year(year, db);
		try {
			Category validCategory = new Category(category, db);
			Diff validDiff = new Diff(diff);
			boolean isValidDiff = db.isValidDiffInPointsMap(validCategory, validDiff, seasonYear);
			if (!isValidDiff) {
				return "redirect:/admin/season/" + year + "/points";
			}

			Points validPoints = new Points(points);
			db.setNewDiffToPointsInPointsMap(validCategory, validDiff, seasonYear, validPoints);
		} catch (EmptyResultDataAccessException e) {
		} catch (InvalidCategoryException e) {
		} catch (InvalidPointsException e) {
		} catch (InvalidDiffException e) {
		}
		return String.format("redirect:/admin/season/%d/points?category=%s&diff=%d&points=%d",
			year, category, diff, points);
	}
}
