package no.vebb.f1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.ScoringTables;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.response.TablesResponse;

/**
 * Class is responsible for showing the scoring system to the users.
 */
@RestController
public class ScoreController {

	@Autowired
	private Database db;

	/**
	 * Handles GET requests for /score. Gives a list of tables showing how scores
	 * are calculated.
	 */
	@GetMapping("/api/public/score")
	public ResponseEntity<TablesResponse> scoreMappingTables(Model model) {
		TablesResponse res = new TablesResponse();
		String title = "Poengberegning";
		res.title = title;
		res.heading = title;
		try {
			res.tables =  ScoringTables.getScoreMappingTables(new Year(TimeUtil.getCurrentYear(), db), db);
		} catch (InvalidYearException e) {
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

}
