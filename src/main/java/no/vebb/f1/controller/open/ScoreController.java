package no.vebb.f1.controller.open;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.scoring.ScoreService;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.year.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@RestController
public class ScoreController {

	private final YearService yearService;
	private final GuessService guessService;
	private final ScoreService scoreService;

	public ScoreController(YearService yearService, GuessService guessService, ScoreService scoreService) {
		this.yearService = yearService;
		this.guessService = guessService;
		this.scoreService = scoreService;
	}

	@GetMapping("/api/public/score")
	public ResponseEntity<Map<Category, Map<Diff, Points>>> scoreMappingTables() {
		try {
			Year year = yearService.getCurrentYear();
			var res = getScoreMappingTables(year);
			return new ResponseEntity<>(res, HttpStatus.OK); 
		} catch (InvalidYearException e) {	
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/api/public/score/{year}")
	public ResponseEntity<Map<Category, Map<Diff, Points>>> scoreMappingTablesYear(@PathVariable("year") int year) {
		try {
			Year validYear = yearService.getYear(year);
			var res = getScoreMappingTables(validYear);
			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (InvalidYearException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private Map<Category, Map<Diff, Points>> getScoreMappingTables(Year year) {
		List<Category> categories = guessService.getCategories();
		Map<Category, Map<Diff, Points>> result = new HashMap<>();
		for (Category category : categories) {
			result.put(category, scoreService.getDiffPointsMap(year, category));
		}
		return result;
	}

}
