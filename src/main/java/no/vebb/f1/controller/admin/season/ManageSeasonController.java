package no.vebb.f1.controller.admin.season;

import java.util.List;

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
        Year seasonYear = new Year(year, db);
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            int maxPos = db.getMaxRaceOrderPosition(seasonYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<RaceId> races = db.getRacesFromSeason(new Year(year, db));
            db.removeRaceOrderFromSeason(seasonYear);
            int currentPos = 1;
            for (RaceId id : races) {
                if (id.equals(validRaceId)) {
                    continue;
                }
                if (currentPos == position) {
                    db.insertRaceOrder(validRaceId, year, currentPos);
                    currentPos++;
                }
                db.insertRaceOrder(id, year, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                db.insertRaceOrder(validRaceId, year, currentPos);
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
        Year seasonYear = new Year(year, db);
        try {
            RaceId validRaceId = new RaceId(raceId, db);
            boolean isRaceInSeason = db.isRaceInSeason(validRaceId, seasonYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            db.deleteRace(validRaceId);

            List<RaceId> races = db.getRacesFromSeason(seasonYear);
            db.removeRaceOrderFromSeason(seasonYear);
            int currentPos = 1;
            for (RaceId id : races) {
                db.insertRaceOrder(id, year, currentPos);
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
        Year seasonYear = new Year(year, db);
        try {
            new RaceId(raceId, db);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidRaceException ignored) {
        }
        importer.importRaceName(raceId, seasonYear);
        importer.importData();
        RaceId validRaceId = new RaceId(raceId, db);
        db.setCutoffRace(cutoff.getDefaultInstant(seasonYear), validRaceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
