package no.vebb.f1.controller.admin.season;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Class is responsible for changing the points system for a specified year.
 */
@RestController
@RequestMapping("/api/admin/season/points")
public class ManagePointsSystemController {

    @Autowired
    private Database db;

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addPointsMapping(
            @RequestParam("year") int year,
            @RequestParam("category") String category) {
        Year seasonYear = new Year(year, db);
        Diff newDiff;
        try {
            Category validCategory = new Category(category, db);
            try {
                newDiff = db.getMaxDiffInPointsMap(seasonYear, validCategory).add(new Diff(1));
            } catch (NullPointerException e) {
                newDiff = new Diff();
            }
            db.addDiffToPointsMap(validCategory, newDiff, seasonYear);
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
        Year seasonYear = new Year(year, db);
        try {
            Category validCategory = new Category(category, db);
            Diff maxDiff = db.getMaxDiffInPointsMap(seasonYear, validCategory);
            db.removeDiffToPointsMap(validCategory, maxDiff, seasonYear);
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
        Year seasonYear = new Year(year, db);
        try {
            Category validCategory = new Category(category, db);
            Diff validDiff = new Diff(diff);
            boolean isValidDiff = db.isValidDiffInPointsMap(validCategory, validDiff, seasonYear);
            if (!isValidDiff) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Points validPoints = new Points(points);
            db.setNewDiffToPointsInPointsMap(validCategory, validDiff, seasonYear, validPoints);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EmptyResultDataAccessException | InvalidCategoryException | InvalidPointsException |
                 InvalidDiffException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
