package no.vebb.f1.controller.admin.season;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.collection.ValuedCompetitor;
import no.vebb.f1.util.domainPrimitive.Color;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidColorException;
import no.vebb.f1.util.exception.InvalidConstructorException;
import no.vebb.f1.util.exception.InvalidDriverException;

@Controller
@RequestMapping("/admin/season/{year}/competitors")
public class SeasonCompetitorsController {

	@Autowired
	private Database db;

	@GetMapping
	public String addSeasonCompetitorsForm(@PathVariable("year") int year, Model model) {
		new Year(year, db);
		model.addAttribute("title", year);
		Map<String, String> linkMap = new LinkedHashMap<>();
		model.addAttribute("linkMap", linkMap);
		String path = String.format("/admin/season/%d/competitors/", year);
		linkMap.put("Konstruktører", path + "constructors");
		linkMap.put("Sjåfører", path + "drivers");
		linkMap.put("Alternative navn", path + "alias");
		return "util/linkList";
	}

	@GetMapping("/drivers")
	public String addDriversForm(@PathVariable("year") int year, Model model) {
		Year seasonYear = new Year(year, db);
		List<ValuedCompetitor<Driver, Constructor>> drivers = db.getDriversTeam(seasonYear);
		List<Constructor> constructors = db.getConstructorsYear(seasonYear);

		model.addAttribute("title", year);
		model.addAttribute("year", year);
		model.addAttribute("drivers", drivers);
		model.addAttribute("constructors", constructors);
		return "admin/addDrivers";
	}

	@PostMapping("/drivers/setTeam")
	@Transactional
	public String setTeamDriver(@PathVariable("year") int year, @RequestParam("driver") String driver,
			@RequestParam("team") String team) {
		Year seasonYear = new Year(year, db);
		try {
			db.setTeamDriver(new Driver(driver, db, seasonYear), new Constructor(team, db, seasonYear), seasonYear);
		} catch (InvalidDriverException e) {
		} catch (InvalidConstructorException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors/drivers#" + driver;
	}

	@PostMapping("/drivers/add")
	@Transactional
	public String addDriverToSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		Year seasonYear = new Year(year, db);
		db.addDriverYear(driver, seasonYear);
		return "redirect:/admin/season/" + year + "/competitors/drivers#add";
	}

	@PostMapping("/drivers/delete")
	@Transactional
	public String removeDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver) {
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
		return "redirect:/admin/season/" + year + "/competitors/drivers";
	}

	@PostMapping("/drivers/move")
	@Transactional
	public String moveDriverFromSeason(@PathVariable("year") int year, @RequestParam("driver") String driver,
			@RequestParam("newPosition") int position) {
		Year seasonYear = new Year(year, db);
		try {
			Driver validDriver = new Driver(driver, db, seasonYear);
			int maxPos = db.getMaxPosDriverYear(seasonYear);
			boolean isPosOutOfBounds = position < 1 || position > maxPos;
			if (isPosOutOfBounds) {
				return "redirect:/admin/season/" + year + "/competitors/drivers#" + driver;
			}

			db.deleteDriverYear(validDriver, seasonYear);
			List<Driver> drivers = db.getDriversYear(seasonYear);
			db.deleteAllDriverYear(seasonYear);

			int currentPos = 1;
			for (Driver currentDriver : drivers) {
				if (currentPos == position) {
					db.addDriverYear(validDriver, seasonYear, currentPos);
					currentPos++;
				}
				db.addDriverYear(currentDriver, seasonYear, currentPos);
				currentPos++;
			}
			if (currentPos == position) {
				db.addDriverYear(validDriver, seasonYear, currentPos);
			}
		} catch (InvalidDriverException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors/drivers#" + driver;
	}

	@GetMapping("/constructors")
	public String addConstructorsForm(@PathVariable("year") int year, Model model) {
		Year seasonYear = new Year(year, db);
		List<ColoredCompetitor<Constructor>> constructors = db.getConstructorsYearWithColors(seasonYear);
		model.addAttribute("title", year);
		model.addAttribute("year", year);
		model.addAttribute("constructors", constructors);
		return "admin/addConstructors";
	}

	@PostMapping("/constructors/add")
	@Transactional
	public String addConstructorToSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
		Year seasonYear = new Year(year, db);
		db.addConstructorYear(constructor, seasonYear);
		return "redirect:/admin/season/" + year + "/competitors/constructors#" + constructor;
	}

	@PostMapping("/constructors/delete")
	@Transactional
	public String removeConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor) {
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
		return "redirect:/admin/season/" + year + "/competitors/constructors";
	}

	@PostMapping("/constructors/move")
	@Transactional
	public String moveConstructorFromSeason(@PathVariable("year") int year,
			@RequestParam("constructor") String constructor, @RequestParam("newPosition") int position) {
		Year seasonYear = new Year(year, db);
		try {
			Constructor validConstructor = new Constructor(constructor, db);
			int maxPos = db.getMaxPosConstructorYear(seasonYear);
			boolean isPosOutOfBounds = position < 1 || position > maxPos;
			if (isPosOutOfBounds) {
				return "redirect:/admin/season/" + year + "/competitors/constructors#" + maxPos;
			}

			db.deleteConstructorYear(validConstructor, seasonYear);
			List<Constructor> constructors = db.getConstructorsYear(seasonYear);
			db.deleteAllConstructorYear(seasonYear);

			int currentPos = 1;
			for (Constructor currentConstructor : constructors) {
				if (currentPos == position) {
					db.addConstructorYear(validConstructor, seasonYear, currentPos);
					currentPos++;
				}
				db.addConstructorYear(currentConstructor, seasonYear, currentPos);
				currentPos++;
			}
			if (currentPos == position) {
				db.addConstructorYear(validConstructor, seasonYear, currentPos);
			}
		} catch (InvalidConstructorException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors/constructors#" + constructor;
	}

	@PostMapping("/constructors/addColor")
	@Transactional
	public String addColorConstructor(@PathVariable("year") int year, @RequestParam("constructor") String constructor,
			@RequestParam("color") String color) {
		Year seasonYear = new Year(year, db);
		try {
			db.addColorConstructor(new Constructor(constructor, db, seasonYear), seasonYear, new Color(color));
		} catch (InvalidConstructorException e) {
		} catch (InvalidColorException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors/constructors#" + constructor;
	}

	@GetMapping("/alias")
	public String addAlternativeNameForm(@PathVariable("year") int year, Model model) {
		Year seasonYear = new Year(year, db);
		List<Driver> drivers = db.getDriversYear(seasonYear);
		Map<String, String> driverAliases = db.getAlternativeDriverNamesYear(seasonYear);
		model.addAttribute("title", String.format("Alternative navn %d", year));
		model.addAttribute("year", year);
		model.addAttribute("drivers", drivers);
		model.addAttribute("driverAliases", driverAliases);
		return "admin/alternativeName";
	}

	@PostMapping("/alias/add")
	@Transactional
	public String addAlternativeName(@PathVariable("year") int year, @RequestParam("driver") String driver,
			@RequestParam("alternativeName") String alternativeName) {
		Year seasonYear = new Year(year, db);
		try {
			Driver validDriver = new Driver(driver, db, seasonYear);
			db.addAlternativeDriverName(validDriver, alternativeName, seasonYear);
		} catch (InvalidDriverException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors/alias";
	}
	
	@PostMapping("/alias/delete")
	@Transactional
	public String deleteAlternativeName(@PathVariable("year") int year, @RequestParam("driver") String driver) {
		Year seasonYear = new Year(year, db);
		try {
			Driver validDriver = new Driver(driver, db, seasonYear);
			db.deleteAlternativeName(validDriver, seasonYear);
		} catch (InvalidDriverException e) {
		}
		return "redirect:/admin/season/" + year + "/competitors/alias";
	}
}
