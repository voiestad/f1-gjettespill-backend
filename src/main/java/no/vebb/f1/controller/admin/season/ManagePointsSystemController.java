package no.vebb.f1.controller.admin.season;

import no.vebb.f1.scoring.ScoreService;
import no.vebb.f1.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.year.Year;
import no.vebb.f1.exception.InvalidPointsException;

@RestController
@RequestMapping("/api/admin/season/points")
public class ManagePointsSystemController {

    private final YearService yearService;
    private final ScoreService scoreService;

    public ManagePointsSystemController(YearService yearService, ScoreService scoreService) {
        this.yearService = yearService;
        this.scoreService = scoreService;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addPointsMapping(
            @RequestParam("year") Year year,
            @RequestParam("category") Category category) {
        if (yearService.isFinishedYear(year)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        Diff newDiff;
        try {
            newDiff = scoreService.getMaxDiffInPointsMap(year, category).add(new Diff(1));
        } catch (NullPointerException e) {
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
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Diff maxDiff = scoreService.getMaxDiffInPointsMap(year, category);
            scoreService.removeDiffToPointsMap(category, maxDiff, year);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/set")
    @Transactional
    public ResponseEntity<?> setPointsMapping(
            @RequestParam("year") Year year,
            @RequestParam("category") Category category,
            @RequestParam("diff") int diff,
            @RequestParam("points") int points) {
        if (yearService.isFinishedYear(year)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Diff validDiff = new Diff(diff);
            boolean isValidDiff = scoreService.isValidDiffInPointsMap(category, validDiff, year);
            if (!isValidDiff) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            UserPoints validPoints = new UserPoints(points);
            scoreService.setNewDiffToPointsInPointsMap(category, validDiff, year, validPoints);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidPointsException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
