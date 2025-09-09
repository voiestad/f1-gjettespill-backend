package no.vebb.f1.controller.admin.season;

import java.util.List;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.util.exception.YearFinishedException;
import no.vebb.f1.year.YearService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.importing.Importer;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidRaceException;

@RestController
@RequestMapping("/api/admin/season/manage")
public class ManageSeasonController {

    private final Importer importer;
    private final CutoffService cutoffService;
    private final YearService yearService;
    private final RaceService raceService;

    public ManageSeasonController(Importer importer, CutoffService cutoffService, YearService yearService, RaceService raceService) {
        this.importer = importer;
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
    }

    @PostMapping("/reload")
    @Transactional
    public ResponseEntity<?> reloadRace(@RequestParam("id") int raceId) {
        try {
            RaceId validRaceId = new RaceId(raceId, raceService);
            Year year = raceService.getYearFromRaceId(validRaceId);
            if (yearService.isFinishedYear(year)) {
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
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            RaceId validRaceId = new RaceId(raceId, raceService);
            boolean isRaceInSeason = raceService.isRaceInSeason(validRaceId, validYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            int maxPos = raceService.getMaxRaceOrderPosition(validYear);
            boolean isPosOutOfBounds = position < 1 || position > maxPos;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<RaceId> races = raceService.getRacesFromSeason(validYear);
            int currentPos = 1;
            for (RaceId id : races) {
                if (id.equals(validRaceId)) {
                    continue;
                }
                if (currentPos == position) {
                    raceService.updateRaceOrderPosition(validRaceId, validYear, currentPos);
                    currentPos++;
                }
                raceService.updateRaceOrderPosition(id, validYear, currentPos);
                currentPos++;
            }
            if (currentPos == position) {
                raceService.updateRaceOrderPosition(validRaceId, validYear, currentPos);
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
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            RaceId validRaceId = new RaceId(raceId, raceService);
            boolean isRaceInSeason = raceService.isRaceInSeason(validRaceId, validYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            raceService.deleteRace(validRaceId);
            List<RaceId> races = raceService.getRacesFromSeason(validYear);
            int currentPos = 1;
            for (RaceId id : races) {
                raceService.updateRaceOrderPosition(id, validYear, currentPos);
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
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            new RaceId(raceId, raceService);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidRaceException ignored) {
        }
        importer.importRaceName(raceId, validYear);
        importer.importData();
        RaceId validRaceId = new RaceId(raceId, raceService);
        cutoffService.setCutoffRace(cutoffService.getDefaultInstant(validYear), validRaceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
