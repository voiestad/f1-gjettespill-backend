package no.vebb.f1.controller.admin.season;

import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.scoring.ScoreService;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.year.Year;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/season/points")
public class ManagePointsSystemController {

    private final YearService yearService;
    private final ScoreService scoreService;
    private final CutoffService cutoffService;

    public ManagePointsSystemController(YearService yearService, ScoreService scoreService, CutoffService cutoffService) {
        this.yearService = yearService;
        this.scoreService = scoreService;
        this.cutoffService = cutoffService;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addPointsMapping(
            @RequestParam("year") Year year,
            @RequestParam("category") Category category) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the point system can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (!cutoffService.isAbleToGuessYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is started and the point system can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Diff> optMaxDiff = scoreService.getMaxDiffInPointsMap(year, category);
        Diff newDiff;
        if (optMaxDiff.isPresent()) {
            newDiff = optMaxDiff.get().add(new Diff(1));
        } else {
            newDiff = new Diff();
        }
        scoreService.addDiffToPointsMap(category, newDiff, year);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deletePointsMapping(
            @RequestParam("year") Year year,
            @RequestParam("category") Category category) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the point system can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (!cutoffService.isAbleToGuessYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is started and the point system can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<Diff> optMaxDiff = scoreService.getMaxDiffInPointsMap(year, category);
        if (optMaxDiff.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        scoreService.removeDiffToPointsMap(category, optMaxDiff.get(), year);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PostMapping("/set")
    @Transactional
    public ResponseEntity<?> setPointsMapping(
            @RequestParam("year") Year year,
            @RequestParam("category") Category category,
            @RequestParam("diff") Diff diff,
            @RequestParam("points") UserPoints points) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the point system can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        if (!cutoffService.isAbleToGuessYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is started and the point system can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        boolean isValidDiff = scoreService.isValidDiffInPointsMap(category, diff, year);
        if (!isValidDiff) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        scoreService.setNewDiffToPointsInPointsMap(category, diff, year, points);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
