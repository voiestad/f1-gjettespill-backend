package no.vebb.f1.controller.admin.season;

import no.vebb.f1.scoring.ScoreService;
import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.guessing.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.year.Year;
import no.vebb.f1.util.exception.InvalidDiffException;
import no.vebb.f1.util.exception.InvalidPointsException;

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
            @RequestParam("year") int year,
            @RequestParam("category") Category category) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        Diff newDiff;
        try {
            newDiff = scoreService.getMaxDiffInPointsMap(validYear, category).add(new Diff(1));
        } catch (NullPointerException e) {
            newDiff = new Diff();
        }
        scoreService.addDiffToPointsMap(category, newDiff, validYear);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deletePointsMapping(
            @RequestParam("year") int year,
            @RequestParam("category") Category category) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Diff maxDiff = scoreService.getMaxDiffInPointsMap(validYear, category);
            scoreService.removeDiffToPointsMap(category, maxDiff, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/set")
    @Transactional
    public ResponseEntity<?> setPointsMapping(
            @RequestParam("year") int year,
            @RequestParam("category") Category category,
            @RequestParam("diff") int diff,
            @RequestParam("points") int points) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Diff validDiff = new Diff(diff);
            boolean isValidDiff = scoreService.isValidDiffInPointsMap(category, validDiff, validYear);
            if (!isValidDiff) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Points validPoints = new Points(points);
            scoreService.setNewDiffToPointsInPointsMap(category, validDiff, validYear, validPoints);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EmptyResultDataAccessException | InvalidPointsException |
                 InvalidDiffException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
