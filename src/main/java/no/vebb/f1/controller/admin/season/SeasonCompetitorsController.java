package no.vebb.f1.controller.admin.season;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/admin/season/{year}/competitors")
public class SeasonCompetitorsController {

	@Autowired
	private UserService userService;

	@Autowired
	private Database db;
	
	@GetMapping
	public String addSeasonCompetitorsForm(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		List<String> drivers = db.getDriversYear(year);
		List<String> constructors = db.getConstructorsYear(year);

		model.addAttribute("title", year);
		model.addAttribute("year", year);
		model.addAttribute("drivers", drivers);
		model.addAttribute("constructors", constructors);
		return "addCompetitors";
	}

	@PostMapping("/addDriver")
	public String addDriverToSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		db.addDriverYear(driver, year);
		
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/addConstructor")
	public String addConstructorToSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		db.addConstructorYear(constructor, year);
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/deleteDriver")
	public String removeDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean driverExists = db.isValidDriverYear(driver, year);
		if (!driverExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}

		db.deleteDriverYear(driver, year);
		List<String> drivers = db.getDriversYear(year);
		db.deleteAllDriverYear(year);

		int position = 1;
		for (String currentDriver : drivers) {
			db.addDriverYear(currentDriver, year, position);
			position++;
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/deleteConstructor")
	public String removeConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean constructorExists = db.isValidConstructorYear(constructor, year);
		if (!constructorExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}

		db.deleteConstructorYear(constructor, year);
		List<String> constructors = db.getConstructorsYear(year);
		db.deleteAllConstructorYear(year);

		int position = 1;
		for (String currentConstructor : constructors) {
			db.addConstructorYear(currentConstructor, year, position);
			position++;
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/moveDriver")
	public String moveDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver,
			@RequestParam("newPosition") int position) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean driverExists = db.isValidDriverYear(driver, year);
		if (!driverExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		int maxPos = db.getMaxPosDriverYear(year);
		boolean isPosOutOfBounds = position < 1 || position > maxPos;
		if (isPosOutOfBounds) {
			return "redirect:/admin/season/" + year + "/competitors";
		}

		db.deleteDriverYear(driver, year);
		List<String> drivers = db.getDriversYear(year);
		db.deleteAllDriverYear(year);

		int currentPos = 1;
		for (String currentDriver : drivers) {
			if (currentPos == position) {
				db.addDriverYear(driver, year, position);
				currentPos++;
			}
			db.addDriverYear(currentDriver, year, position);
			currentPos++;
		}
		if (currentPos == position) {
			db.addDriverYear(driver, year, position);
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/moveConstructor")
	public String moveConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor, @RequestParam("newPosition") int position) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		boolean isValidYear = db.isValidSeason(year);
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		boolean constructorExists = db.isValidConstructorYear(constructor, year);
		if (!constructorExists) {
			return "redirect:/admin/season/" + year + "/competitors";
		}
		int maxPos = db.getMaxPosConstructorYear(year);
		boolean isPosOutOfBounds = position < 1 || position > maxPos;
		if (isPosOutOfBounds) {
			return "redirect:/admin/season/" + year + "/competitors";
		}

		db.deleteConstructorYear(constructor, year);
		List<String> constructors = db.getConstructorsYear(year);
		db.deleteAllConstructorYear(year);

		int currentPos = 1;
		for (String currentConstructor : constructors) {
			if (currentPos == position) {
				db.addConstructorYear(constructor, year, position);
				currentPos++;
			}
			db.addConstructorYear(currentConstructor, year, position);
			currentPos++;
		}
		if (currentPos == position) {
			db.addConstructorYear(constructor, year, position);
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}
}
