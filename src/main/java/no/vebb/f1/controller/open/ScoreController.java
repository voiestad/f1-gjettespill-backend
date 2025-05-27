package no.vebb.f1.controller.open;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.ScoringTables;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

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
	public ResponseEntity<Map<Category, Map<Diff, Points>>> scoreMappingTables(Model model) {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			var res = ScoringTables.getScoreMappingTables(year, db);
			return new ResponseEntity<>(res, HttpStatus.OK); 
		} catch (InvalidYearException e) {	
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}

}
