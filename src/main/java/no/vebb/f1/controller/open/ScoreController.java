package no.vebb.f1.controller.open;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class ScoreController {

	private final Database db;
	private final YearService yearService;

	public ScoreController(Database db, YearService yearService) {
		this.db = db;
		this.yearService = yearService;
	}

	@GetMapping("/api/public/score")
	public ResponseEntity<Map<Category, Map<Diff, Points>>> scoreMappingTables() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			var res = getScoreMappingTables(year, db);
			return new ResponseEntity<>(res, HttpStatus.OK); 
		} catch (InvalidYearException e) {	
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/api/public/score/{year}")
	public ResponseEntity<Map<Category, Map<Diff, Points>>> scoreMappingTablesYear(@PathVariable("year") int year) {
		try {
			Year validYear = new Year(year, yearService);
			var res = getScoreMappingTables(validYear, db);
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private Map<Category, Map<Diff, Points>> getScoreMappingTables(Year year, Database db) {
		List<Category> categories = db.getCategories();
		Map<Category, Map<Diff, Points>> result = new HashMap<>();
		for (Category category : categories) {
			result.put(category, db.getDiffPointsMap(year, category)); 
		}
		return result;
	}

}
