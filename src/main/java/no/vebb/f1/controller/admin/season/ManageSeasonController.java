package no.vebb.f1.controller.admin.season;

import java.util.List;

import no.vebb.f1.race.RacePosition;
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
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
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
            RaceId validRaceId = raceService.getRaceId(raceId);
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
            @RequestParam("newPosition") int inputPosition) {
        Year validYear = yearService.getYear(year);
        if (yearService.isFinishedYear(validYear)) {
            throw new YearFinishedException("Year '" + year + "' is over and the race can't be changed");
        }
        try {
            RaceId validRaceId = raceService.getRaceId(raceId);
            boolean isRaceInSeason = raceService.isRaceInSeason(validRaceId, validYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            RacePosition maxPos = raceService.getNewMaxRaceOrderPosition(validYear);
            boolean isPosOutOfBounds = inputPosition < 1 || inputPosition > maxPos.toValue() - 1;
            if (isPosOutOfBounds) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<RaceId> races = raceService.getRacesFromSeason(validYear);
            RacePosition currentPos = new RacePosition();
            RacePosition position = new RacePosition(inputPosition);
            for (RaceId id : races) {
                if (id.equals(validRaceId)) {
                    continue;
                }
                if (currentPos.equals(position)) {
                    raceService.updateRaceOrderPosition(validRaceId, validYear, currentPos);
                    currentPos = currentPos.next();
                }
                raceService.updateRaceOrderPosition(id, validYear, currentPos);
                currentPos = currentPos.next();

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
            RaceId validRaceId = raceService.getRaceId(raceId);
            boolean isRaceInSeason = raceService.isRaceInSeason(validRaceId, validYear);
            if (!isRaceInSeason) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            raceService.deleteRace(validRaceId);
            List<RaceId> races = raceService.getRacesFromSeason(validYear);
            RacePosition currentPos = new RacePosition();
            for (RaceId id : races) {
                raceService.updateRaceOrderPosition(id, validYear, currentPos);
                currentPos = currentPos.next();
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
            raceService.getRaceId(raceId);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (InvalidRaceException ignored) {
        }
        importer.importRaceName(raceId, validYear);
        importer.importData();
        RaceId validRaceId = raceService.getRaceId(raceId);
        cutoffService.setCutoffRace(cutoffService.getDefaultInstant(validYear), validRaceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
