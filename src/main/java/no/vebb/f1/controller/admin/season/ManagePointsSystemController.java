package no.vebb.f1.controller.admin.season;

import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidCategoryException;
import no.vebb.f1.util.exception.InvalidDiffException;
import no.vebb.f1.util.exception.InvalidPointsException;

@RestController
@RequestMapping("/api/admin/season/points")
public class ManagePointsSystemController {

    private final Database db;
    private final YearService yearService;

    public ManagePointsSystemController(Database db, YearService yearService) {
        this.db = db;
        this.yearService = yearService;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addPointsMapping(
            @RequestParam("year") int year,
            @RequestParam("category") String category) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        Diff newDiff;
        try {
            Category validCategory = new Category(category, db);
            try {
                newDiff = db.getMaxDiffInPointsMap(validYear, validCategory).add(new Diff(1));
            } catch (NullPointerException e) {
                newDiff = new Diff();
            }
            db.addDiffToPointsMap(validCategory, newDiff, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidCategoryException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deletePointsMapping(
            @RequestParam("year") int year,
            @RequestParam("category") String category) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Category validCategory = new Category(category, db);
            Diff maxDiff = db.getMaxDiffInPointsMap(validYear, validCategory);
            db.removeDiffToPointsMap(validCategory, maxDiff, validYear);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NullPointerException | InvalidCategoryException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/set")
    @Transactional
    public ResponseEntity<?> setPointsMapping(
            @RequestParam("year") int year,
            @RequestParam("category") String category,
            @RequestParam("diff") int diff,
            @RequestParam("points") int points) {
        Year validYear = new Year(year, yearService);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            Category validCategory = new Category(category, db);
            Diff validDiff = new Diff(diff);
            boolean isValidDiff = db.isValidDiffInPointsMap(validCategory, validDiff, validYear);
            if (!isValidDiff) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Points validPoints = new Points(points);
            db.setNewDiffToPointsInPointsMap(validCategory, validDiff, validYear, validPoints);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EmptyResultDataAccessException | InvalidCategoryException | InvalidPointsException |
                 InvalidDiffException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
