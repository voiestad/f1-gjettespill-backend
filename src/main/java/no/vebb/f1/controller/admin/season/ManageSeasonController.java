package no.vebb.f1.controller.admin.season;

import java.util.List;
import java.util.Optional;

import no.vebb.f1.race.RacePosition;
import no.vebb.f1.race.RaceService;
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
    public ResponseEntity<?> reloadRace(@RequestParam("id") RaceId raceId) {
        Optional<Year> optYear = raceService.getYearFromRaceId(raceId);
        if (optYear.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Year year = optYear.get();
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        importer.importRaceData(raceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/move")
    @Transactional
    public ResponseEntity<?> changeRaceOrder(
            @RequestParam("year") Year year,
            @RequestParam("id") RaceId raceId,
            @RequestParam("newPosition") int inputPosition) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        boolean isRaceInSeason = raceService.isRaceInSeason(raceId, year);
        if (!isRaceInSeason) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        RacePosition maxPos = raceService.getNewMaxRaceOrderPosition(year);
        boolean isPosOutOfBounds = inputPosition < 1 || inputPosition > maxPos.toValue() - 1;
        if (isPosOutOfBounds) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<RaceId> races = raceService.getRacesFromSeason(year);
        RacePosition currentPos = new RacePosition();
        RacePosition position = RacePosition.getRacePosition(inputPosition).orElseThrow(RuntimeException::new);
        // TODO: update all at once
        for (RaceId id : races) {
            if (id.equals(raceId)) {
                continue;
            }
            if (currentPos.equals(position)) {
                raceService.updateRaceOrderPosition(raceId, year, currentPos);
                currentPos = currentPos.next();
            }
            raceService.updateRaceOrderPosition(id, year, currentPos);
            currentPos = currentPos.next();
        }
        if (currentPos == position) {
            raceService.updateRaceOrderPosition(raceId, year, currentPos);
        }
        importer.importData();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteRace(@RequestParam("year") Year year, @RequestParam("id") RaceId raceId) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        boolean isRaceInSeason = raceService.isRaceInSeason(raceId, year);
        if (!isRaceInSeason) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        raceService.deleteRace(raceId);
        List<RaceId> races = raceService.getRacesFromSeason(year);
        RacePosition currentPos = new RacePosition();
        for (RaceId id : races) {
            raceService.updateRaceOrderPosition(id, year, currentPos);
            currentPos = currentPos.next();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addRace(@RequestParam("year") Year year, @RequestParam("id") int inputRaceId) {
        if (yearService.isFinishedYear(year)) {
            return new ResponseEntity<>("Year '" + year + "' is over and the race can't be changed",
                    HttpStatus.FORBIDDEN);
        }
        Optional<RaceId> optRaceId = raceService.getRaceId(inputRaceId);
        if (optRaceId.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        importer.importRaceName(inputRaceId, year);
        importer.importData();
        Optional<RaceId> raceId = raceService.getRaceId(inputRaceId);
        if (raceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        cutoffService.setCutoffRace(cutoffService.getDefaultInstant(year), raceId.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
