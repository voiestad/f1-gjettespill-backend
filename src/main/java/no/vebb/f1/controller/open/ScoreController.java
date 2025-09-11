package no.vebb.f1.controller.open;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.scoring.ScoreService;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.year.Year;

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
    public ResponseEntity<Map<Category, Map<Diff, UserPoints>>> scoreMappingTables() {
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var res = getScoreMappingTables(optYear.get());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/api/public/score/{year}")
    public ResponseEntity<Map<Category, Map<Diff, UserPoints>>> scoreMappingTablesYear(@PathVariable("year") Year year) {
        var res = getScoreMappingTables(year);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private Map<Category, Map<Diff, UserPoints>> getScoreMappingTables(Year year) {
        List<Category> categories = guessService.getCategories();
        Map<Category, Map<Diff, UserPoints>> result = new HashMap<>();
        for (Category category : categories) {
            result.put(category, scoreService.getDiffPointsMap(year, category));
        }
        return result;
    }

}
