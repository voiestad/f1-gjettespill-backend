package no.vebb.f1.controller.admin.season;

import java.util.List;

import no.vebb.f1.util.exception.YearFinishedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.importing.Importer;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;

@RestController
@RequestMapping("/api/admin/season/manage")
public class ManageSeasonController {

    private final Database db;
    private final Importer importer;
    private final Cutoff cutoff;

    public ManageSeasonController(Database db, Importer importer, Cutoff cutoff) {
        this.db = db;
        this.importer = importer;
        this.cutoff = cutoff;
    }

    @PostMapping("/reload")
    @Transactional
    public ResponseEntity<?> reloadRace(@RequestParam("id") int raceId) {
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            Year year = db.getYearFromRaceId(validRaceId);
            if (db.isFinishedYear(year)) {
                throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
            }
            importer.importRaceData(validRaceId);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
    }

    @PostMapping("/move")
    @Transactional
    public ResponseEntity<?> changeRaceOrder(
            @RequestParam("year") int year,
            @RequestParam("id") int raceId,
            @RequestParam("newPosition") int position) {
        Year validYear = new Year(year, db);
        if (db.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            boolean isRaceInSeason = db.isRaceInSeason(validRaceId, validYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            int maxPos = db.getMaxRaceOrderPosition(validYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<RaceId> races = db.getRacesFromSeason(validYear);
            db.removeRaceOrderFromSeason(validYear);
            int currentPos = 1;
            for (RaceId id : races) {
                if (id.equals(validRaceId)) {
                    continue;
                }
                if (currentPos == position) {
                    db.insertRaceOrder(validRaceId, validYear, currentPos);
                    currentPos++;
                }
                db.insertRaceOrder(id, validYear, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                db.insertRaceOrder(validRaceId, validYear, currentPos);
            }
            importer.importData();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteRace(@RequestParam("year") int year, @RequestParam("id") int raceId) {
        Year validYear = new Year(year, db);
        if (db.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            boolean isRaceInSeason = db.isRaceInSeason(validRaceId, validYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            db.deleteRace(validRaceId);

            List<RaceId> races = db.getRacesFromSeason(validYear);
            db.removeRaceOrderFromSeason(validYear);
            int currentPos = 1;
            for (RaceId id : races) {
                db.insertRaceOrder(id, validYear, currentPos);
                currentPos++;
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidRaceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addRace(@RequestParam("year") int year, @RequestParam("id") int raceId) {
        Year validYear = new Year(year, db);
        if (db.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            new RaceId(raceId, db);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidRaceException ignored) {
        }
        importer.importRaceName(raceId, validYear);
        importer.importData();
        RaceId validRaceId = new RaceId(raceId, db);
        db.setCutoffRace(cutoff.getDefaultInstant(validYear), validRaceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
