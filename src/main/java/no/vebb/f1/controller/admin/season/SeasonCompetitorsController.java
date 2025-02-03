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
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidConstructorException;
import no.vebb.f1.util.exception.InvalidDriverException;

@Controller
@RequestMapping("/admin/season/{year}/competitors")
public class SeasonCompetitorsController {

	@Autowired
	private UserService userService;

	@Autowired
	private Database db;
	
	@GetMapping
	public String addSeasonCompetitorsForm(@PathVariable("year") int year, Model model) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		List<Driver> drivers = db.getDriversYear(seasonYear);
		List<Constructor> constructors = db.getConstructorsYear(seasonYear);

		model.addAttribute("title", year);
		model.addAttribute("year", year);
		model.addAttribute("drivers", drivers);
		model.addAttribute("constructors", constructors);
		return "addCompetitors";
	}

	@PostMapping("/addDriver")
	public String addDriverToSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);

		db.addDriverYear(driver, seasonYear);
		
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/addConstructor")
	public String addConstructorToSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		db.addConstructorYear(constructor, seasonYear);
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/deleteDriver")
	public String removeDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		try {
			Driver validDriver = new Driver(driver, db, seasonYear);

			db.deleteDriverYear(validDriver, seasonYear);
			List<Driver> drivers = db.getDriversYear(seasonYear);
			db.deleteAllDriverYear(seasonYear);

			int position = 1;
			for (Driver currentDriver : drivers) {
				db.addDriverYear(currentDriver, seasonYear, position);
				position++;
			}
		} catch (InvalidDriverException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/deleteConstructor")
	public String removeConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		try {
			Constructor validConstructor = new Constructor(constructor, db);

			db.deleteConstructorYear(validConstructor, seasonYear);
			List<Constructor> constructors = db.getConstructorsYear(seasonYear);
			db.deleteAllConstructorYear(seasonYear);

			int position = 1;
			for (Constructor currentConstructor : constructors) {
				db.addConstructorYear(currentConstructor, seasonYear, position);
				position++;
			}
		} catch (InvalidConstructorException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/moveDriver")
	public String moveDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver,
			@RequestParam("newPosition") int position) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		try {
			Driver validDriver = new Driver(driver, db, seasonYear);
			int maxPos = db.getMaxPosDriverYear(seasonYear);
			boolean isPosOutOfBounds = position < 1 || position > maxPos;
			if (isPosOutOfBounds) {
				return "redirect:/admin/season/" + year + "/competitors";
			}

			db.deleteDriverYear(validDriver, seasonYear);
			List<Driver> drivers = db.getDriversYear(seasonYear);
			db.deleteAllDriverYear(seasonYear);

			int currentPos = 1;
			for (Driver currentDriver : drivers) {
				if (currentPos == position) {
					db.addDriverYear(validDriver, seasonYear, position);
					currentPos++;
				}
				db.addDriverYear(currentDriver, seasonYear, position);
				currentPos++;
			}
			if (currentPos == position) {
				db.addDriverYear(validDriver, seasonYear, position);
			}
		} catch (InvalidDriverException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}

	@PostMapping("/moveConstructor")
	public String moveConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor, @RequestParam("newPosition") int position) {
		userService.adminCheck();
		Year seasonYear = new Year(year, db);
		try {
			Constructor validConstructor = new Constructor(constructor, db);
			int maxPos = db.getMaxPosConstructorYear(seasonYear);
			boolean isPosOutOfBounds = position < 1 || position > maxPos;
			if (isPosOutOfBounds) {
				return "redirect:/admin/season/" + year + "/competitors";
			}

			db.deleteConstructorYear(validConstructor, seasonYear);
			List<Constructor> constructors = db.getConstructorsYear(seasonYear);
			db.deleteAllConstructorYear(seasonYear);

			int currentPos = 1;
			for (Constructor currentConstructor : constructors) {
				if (currentPos == position) {
					db.addConstructorYear(validConstructor, seasonYear, position);
					currentPos++;
				}
				db.addConstructorYear(currentConstructor, seasonYear, position);
				currentPos++;
			}
			if (currentPos == position) {
				db.addConstructorYear(validConstructor, seasonYear, position);
			}
		} catch (InvalidConstructorException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors";
	}
}
