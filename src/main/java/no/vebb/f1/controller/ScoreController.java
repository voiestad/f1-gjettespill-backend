package no.vebb.f1.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.ScoringTables;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

/**
 * Class is responsible for showing the scoring system to the users.
 */
@Controller
@RequestMapping("/score")
public class ScoreController {

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	/**
	 * Handles GET requests for /score. Gives a list of tables showing how scores
	 * are calculated.
	 */
	@GetMapping
	public String scoreMappingTables(Model model) {
		List<Table> scoreMappingTables;
		try {
			scoreMappingTables = ScoringTables.getScoreMappingTables(new Year(TimeUtil.getCurrentYear(), db), db);
		} catch (InvalidYearException e) {
			scoreMappingTables = Arrays.asList();
		}
		model.addAttribute("tables", scoreMappingTables);
		model.addAttribute("title", "Poengberegning");
		model.addAttribute("loggedOut", !userService.isLoggedIn());
		return "util/tables";
	}

}
