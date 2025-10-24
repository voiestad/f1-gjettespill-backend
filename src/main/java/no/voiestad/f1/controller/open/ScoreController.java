package no.voiestad.f1.controller.open;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.scoring.ScoreService;
import no.voiestad.f1.year.YearService;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.scoring.domain.Diff;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.year.Year;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
        return yearService.getCurrentYear()
                .map(this::scoreMappingTablesYear)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/api/public/score/{year}")
    public ResponseEntity<Map<Category, Map<Diff, UserPoints>>> scoreMappingTablesYear(@PathVariable("year") Year year) {
        return new ResponseEntity<>(getScoreMappingTables(year), HttpStatus.OK);
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
