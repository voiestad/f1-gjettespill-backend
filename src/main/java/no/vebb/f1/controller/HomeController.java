package no.vebb.f1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.graph.GraphCache;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

import org.springframework.ui.Model;

/**
 * Controller for home and contact page.
 */
@Controller
public class HomeController {

	@Autowired
	private Database db;

	@Autowired
	private GraphCache graphCache;

	/**
	 * Handles GET request for home page.
	 * 
	 * @param model
	 * @return home page
	 */
	@GetMapping("/")
	public String home(Model model) {
		Table leaderboard = graphCache.getleaderboard();
		model.addAttribute("leaderboard", leaderboard);
		try {
			if (leaderboard.getHeader().size() == 0) {
				Year year = new Year(TimeUtil.getCurrentYear(), db);
				List<String> guessers = db.getSeasonGuessers(year);
				model.addAttribute("guessers", guessers);
			} else {
				model.addAttribute("graph", graphCache.getGraph());	
			}
		} catch (InvalidYearException e) {
		}
		return "public/index";
	}

	/**
	 * Handles GET request for contact page.
	 * 
	 * @return file for contact page
	 */
	@GetMapping("/contact")
	public String contact() {
		return "public/contact";
	}
	
	/**
	 * Handles GET request for about page.
	 */
	@GetMapping("/about")
	public String about() {
		return "public/about";
	}

	/**
	 * Handles GET request for privacy page.
	 */
	@GetMapping("/privacy")
	public String privacy() {
		return "public/privacy";
	}

}
